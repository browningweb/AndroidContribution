/*
 * encode_mp3.c
 *
 *  Created on: 04-Jan-2014
 *      Author: mohit
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include <encode_mp3.h>
#include <jni.h>

#include "libavutil/opt.h"
#include "libavutil/mem.h"

#include "libavutil/mathematics.h"
#include "libavutil/samplefmt.h"



#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libavresample/avresample.h"

#include <android/log.h>
#define TAG "LibAv"


#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)


#define ERROR_CODE -22 /* 25 images/s */



uint8_t *audio_outbuf;
int audio_outbuf_size;
int audio_input_frame_size;

static uint8_t **src_samples_data;
static int       src_samples_linesize;
static int       src_nb_samples;

static int max_dst_nb_samples;
uint8_t **dst_samples_data;
int       dst_samples_linesize;
int       dst_samples_size;

uint8_t * finalFramebuffer;



// init default values

AVOutputFormat *fmt;
AVFormatContext *oc;
AVStream *audio_st;
AVCodec *audio_codec;

double audio_time;


/* Add an output stream. */
static int add_stream(AVFormatContext *oc, AVCodec **codec,
                            enum AVCodecID codec_id,  AVStream **st)
{
    AVCodecContext *c;

    /* find the encoder */
    *codec = avcodec_find_encoder(codec_id);
    if (!(*codec)) {
        LOGV("Could not find encoder for");
        exit(1);
    }

    *st = avformat_new_stream(oc, *codec);
    if (!st) {
        LOGE("Could not allocate stream\n");
        exit(1);
    }
    (*st)->id = oc->nb_streams-1;
    c = (*st)->codec;

    switch ((*codec)->type) {
    case AVMEDIA_TYPE_AUDIO:
        c->sample_fmt  = AV_SAMPLE_FMT_S16P;
        c->bit_rate    = 128000;
		c->sample_rate = 44100;//select_sample_rate(*codec);
		c->channel_layout = AV_CH_LAYOUT_MONO;//select_channel_layout(*codec);
		c->time_base = (AVRational){1, c->sample_rate};
		c->channels = av_get_channel_layout_nb_channels(c->channel_layout);
    break;

    default:
        break;
    }

    /* Some formats want stream headers to be separate. */
    if (oc->oformat->flags & AVFMT_GLOBALHEADER)
        c->flags |= CODEC_FLAG_GLOBAL_HEADER;

    return 0;
}

/**************************************************************/
/* audio output */


//------------------------


struct AVAudioResampleContext *swr_ctx = NULL;


int static av_samples_alloc_array_and_samples(uint8_t ***audio_data, int *linesize, int nb_channels,
                                       int nb_samples, enum AVSampleFormat sample_fmt, int align)
{
    int ret, nb_planes = av_sample_fmt_is_planar(sample_fmt) ? nb_channels : 1;

    *audio_data = av_malloc(nb_planes * sizeof(**audio_data));
    memset(*audio_data, 0, nb_planes * sizeof(**audio_data));
    if (!*audio_data)
        return AVERROR(ENOMEM);
    ret = av_samples_alloc(*audio_data, linesize, nb_channels,
                           nb_samples, sample_fmt, align);
    if (ret < 0)
        av_freep(audio_data);
    return ret;
}



static void open_audio(AVFormatContext *oc,  AVCodec *audio_codec, AVStream *st)
{
    AVCodecContext *c;
    AVCodec *codec;
    int ret;
    c = st->codec;
    c->strict_std_compliance = -2;
    /* open it */
    ret = avcodec_open2(c, audio_codec, NULL);
    if (ret == AVERROR_EXPERIMENTAL) {
    	 LOGE( "experimental codec\n");
	}
    if (ret < 0) {
       LOGE( "could not open codec\n");
        exit(1);
    }


    audio_outbuf_size = 10000;
    audio_outbuf = av_malloc(audio_outbuf_size);

    /* ugly hack for PCM codecs (will be removed ASAP with new PCM
       support to compute the input frame size in samples */
    if (c->frame_size <= 1) {
        audio_input_frame_size = audio_outbuf_size / c->channels;
        switch(st->codec->codec_id) {
        case AV_CODEC_ID_PCM_S16LE:
        case AV_CODEC_ID_PCM_S16BE:
        case AV_CODEC_ID_PCM_U16LE:
        case AV_CODEC_ID_PCM_U16BE:
            audio_input_frame_size >>= 1;
            break;
        default:
            break;
        }
    } else {
        audio_input_frame_size = c->frame_size;
    }


    // init resampling
    src_nb_samples = c->codec->capabilities & CODEC_CAP_VARIABLE_FRAME_SIZE ?10000 : c->frame_size;

	ret = av_samples_alloc_array_and_samples(&src_samples_data,
			&src_samples_linesize, c->channels, src_nb_samples, AV_SAMPLE_FMT_S16,0);
	if (ret < 0) {
		LOGE("Could not allocate source samples\n");
		exit(1);
	}

       /* create resampler context */
       if (c->sample_fmt != AV_SAMPLE_FMT_S16) {
           swr_ctx =  avresample_alloc_context();
           if (!swr_ctx) {
        	   LOGE("Could not allocate resampler context\n");
               exit(1);
           }

           /* set options */
           av_opt_set_int       (swr_ctx, "in_channel_count",   c->channels,       0);
           av_opt_set_int       (swr_ctx, "in_channel_layout",   c->channel_layout,       0);
           av_opt_set_int       (swr_ctx, "in_sample_rate",     c->sample_rate,    0);
           av_opt_set_int		(swr_ctx, "in_sample_fmt",      AV_SAMPLE_FMT_S16, 0);
           av_opt_set_int       (swr_ctx, "out_channel_count",  c->channels,       0);
           av_opt_set_int       (swr_ctx, "out_sample_rate",    c->sample_rate,    0);
           av_opt_set_int       (swr_ctx, "out_channel_layout",   c->channel_layout,       0);
           av_opt_set_int		(swr_ctx, "out_sample_fmt",     c->sample_fmt, 0);//av_opt_set_sample_fmt


           /* initialize the resampling context */
           if ((ret =  avresample_open(swr_ctx)) < 0) {
        	   LOGE("Failed to initialize the resampling context\n");
               exit(1);
           }
       }

       /* compute the number of converted samples: buffering is avoided
        * ensuring that the output buffer will contain at least all the
        * converted input samples */
       max_dst_nb_samples = src_nb_samples;
       ret = av_samples_alloc_array_and_samples(&dst_samples_data, &dst_samples_linesize, c->channels,
                                                max_dst_nb_samples, c->sample_fmt, 0);
       if (ret < 0) {
           LOGE("Could not allocate destination samples\n");
           exit(1);
       }
       dst_samples_size = av_samples_get_buffer_size(NULL, c->channels, max_dst_nb_samples,
                                                     c->sample_fmt, 0);

}



void Java_com_example_libavndkdemo_Mp3Encoder_writeAudioFrame(JNIEnv* env, jobject this, jshortArray inSample, jint length)
{

	audio_time = audio_st ? audio_st->pts.val * av_q2d(audio_st->time_base) : 0.0;

	int got_packet, ret, dst_nb_samples;

	AVStream *st = audio_st;
	AVPacket pkt;
	AVCodecContext *c = st->codec;

	AVFrame *audioframe = av_frame_alloc();
	audioframe->nb_samples = c->frame_size;
	audioframe->format = c->sample_fmt;
	audioframe->channel_layout = c->channel_layout;

	jshort* bufferShortPtr = (*env)->GetShortArrayElements(env, inSample,NULL);
	jbyte* bufferPtr = (jbyte *) bufferShortPtr;

	int buffer_size = av_samples_get_buffer_size(NULL, c->channels,
			c->frame_size, c->sample_fmt, 0);

	memcpy((int16_t *) src_samples_data[0], bufferShortPtr, length * 2);

	/* convert samples from native format to destination codec format, using the resampler */
	if (swr_ctx) {
		/* compute destination number of samples */
		dst_nb_samples = av_rescale_rnd(
				avresample_get_delay(swr_ctx) + src_nb_samples,
				c->sample_rate, c->sample_rate, AV_ROUND_UP);
		if (dst_nb_samples > max_dst_nb_samples) {
			av_free(dst_samples_data[0]);
			ret = av_samples_alloc(dst_samples_data, &dst_samples_linesize,
					c->channels, dst_nb_samples, c->sample_fmt, 0);
			if (ret < 0)
				exit(1);
			max_dst_nb_samples = dst_nb_samples;
			dst_samples_size = av_samples_get_buffer_size(NULL, c->channels,
					dst_nb_samples, c->sample_fmt, 0);
		}

		/* convert to destination format */
		ret = avresample_convert(swr_ctx, dst_samples_data, 0,
				dst_nb_samples, (uint8_t **) src_samples_data, 0,
				src_nb_samples);

		if (ret < 0) {
			LOGE("Error while converting\n");
			exit(1);
		}
	} else {
		dst_samples_data[0] = src_samples_data[0];
		dst_nb_samples = src_nb_samples;
	}

	audioframe->nb_samples = dst_nb_samples;
	/* setup the data pointers in the AVFrame */
	ret = avcodec_fill_audio_frame(audioframe, c->channels, c->sample_fmt,
			dst_samples_data[0], dst_samples_size, 0);

	av_init_packet(&pkt);
	pkt.data = NULL; // packet data will be allocated by the encoder
	pkt.size = 0;

	if (avcodec_encode_audio2(c, &pkt, audioframe, &got_packet) < 0) {
		LOGE("Error encoding audio frame");
		exit(1);
	}

	if (got_packet) {
		pkt.stream_index = st->index;
		if (pkt.pts != AV_NOPTS_VALUE)
			pkt.pts = av_rescale_q(pkt.pts, st->codec->time_base,st->time_base);
		if (pkt.dts != AV_NOPTS_VALUE)
			pkt.dts = av_rescale_q(pkt.dts, st->codec->time_base,st->time_base);
		if (c && c->coded_frame && c->coded_frame->key_frame)
			pkt.flags |= AV_PKT_FLAG_KEY;

		/* Write the compressed frame to the media file. */
		ret = av_interleaved_write_frame(oc, &pkt);
		av_free_packet(&pkt);
	}else{
		LOGV("Audio Frame Buffered");
	}
	//	av_freep(&samples);
	av_frame_free(&audioframe);
	(*env)->ReleaseShortArrayElements(env, inSample, bufferShortPtr, 0);


}



static void close_audio(AVFormatContext *oc, AVStream *st)
{
    avcodec_close(st->codec);
    av_free(audio_outbuf);
    av_free(src_samples_data[0]);
    av_free(dst_samples_data[0]);
}


/**************************************************************/

/**
 * Method initialize native code
 * @param env JNIEnv
 * @param obj jobject
 * @param p_filename jstring filePath
 * @return 0 for success or ERROR_CODE -22
 */
int Java_com_example_libavndkdemo_Mp3Encoder_initAudio(JNIEnv* env, jobject obj, jstring filePath)
{
    int ret;

    /* Initialize libavcodec, and register all codecs and formats. */

    avcodec_register_all();
    av_register_all();
    avfilter_register_all();

    // init vars for cropping frame

    const char *filename = (*env)->GetStringUTFChars(env,filePath, NULL);

    fmt = av_guess_format(NULL, filename, NULL);
       if (!fmt) {
           LOGE("Could not deduce output format from file extension: using MPEG.\n");
           fmt = av_guess_format("mp3", NULL, NULL);
       }
       if (!fmt) {
          LOGE("Could not find suitable output format\n");
           return ERROR_CODE;
       }

    /* allocate the output media context */
    oc = avformat_alloc_context();
    oc->oformat = fmt;
    if (!oc) {
        LOGV("Could not deduce output format from file extension: using MPEG.\n");
        return ERROR_CODE;
    }

    fmt = oc->oformat;


    /* Add the audio stream using the default format codecs
     * and initialize the codecs. */
    audio_st = NULL;

    if (fmt->audio_codec != AV_CODEC_ID_NONE) {
        ret = add_stream(oc, &audio_codec, fmt->audio_codec, &audio_st);
    }


    /* Now that all the parameters are set, we can open the audio
     *  codecs and allocate the necessary encode buffers. */
    if (audio_st)
        open_audio(oc, audio_codec, audio_st);

    av_dump_format(oc, 0, filename, 1);

    /* open the output file, if needed */
    if (!(fmt->flags & AVFMT_NOFILE)) {
        ret = avio_open(&oc->pb, filename, AVIO_FLAG_WRITE);
        if (ret < 0) {
            LOGE("Could not open '%s'", filename);
            return ERROR_CODE;
        }
    }

    /* Write the stream header, if any. */
    ret = avformat_write_header(oc, NULL);
    if (ret < 0) {
       LOGE("Error occurred when opening output file");
       return ERROR_CODE;
    }

    LOGV("Encoding initialized");
    (*env)->ReleaseStringUTFChars(env, filePath, filename);
    return 0;
}


int Java_com_example_libavndkdemo_Mp3Encoder_getFrameSize(){
	if (audio_st) {
		return audio_st->codec->frame_size;
	}

	return 0;
}


int Java_com_example_libavndkdemo_Mp3Encoder_close(JNIEnv* env, jobject obj){

	    /* Write the trailer, if any. The trailer must be written before you
	     * close the CodecContexts open when you wrote the header; otherwise
	     * av_write_trailer() may try to use memory that was freed on
	     * av_codec_close(). */
	    LOGV("writing trailer");
	    av_write_trailer(oc);

	    /* Close each codec. */
	    if (audio_st)
	        close_audio(oc, audio_st);

	    if (!(fmt->flags & AVFMT_NOFILE))
	        /* Close the output file. */
	        avio_close(oc->pb);

	    /* free the stream */
	    avformat_free_context(oc);


	    return 0;

}

