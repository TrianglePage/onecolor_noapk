#include<jni.h>
#include<android/log.h>
#include<opencv2/opencv.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/legacy/legacy.hpp>
#include <vector>
#include <math.h>
#include "stdlib.h"
#include "stdio.h"
#include <stack>
#include "bits/stl_stack.h"
#define TAG    "jni_opencv"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)

using namespace cv;
using namespace std;

extern "C" {

static IplImage* full_mask;
static int touch_count = 0;

Mat RegionGrow(Mat src, CvPoint pt, int th);
typedef enum
{
	gray =0,
	GREEN=1,
	BLUE =2,
	YELLOW=3,
	PURPLE=4,
};
void Grow(IplImage* src,IplImage* seed, int t1);
JNIEXPORT jintArray JNICALL Java_com_puzzleworld_onecolor_ImageProcesser_ImgFun(
		JNIEnv* env, jobject obj, jintArray buf, int w, int h, jintArray touchPoints, int touchPointsCount, int level, int bgColor, int bgBlur);

JNIEXPORT jintArray JNICALL Java_com_puzzleworld_onecolor_ImageProcesser_ImgFun(
		JNIEnv* env, jobject obj, jintArray buf, int w, int h, jintArray touchPoints, int touchPointsCount, int level, int bgColor, int bgBlur) {
	jint *cbuf;
	cbuf = env->GetIntArrayElements(buf, NULL);
	if (cbuf == NULL) {
		return 0;
	}

	jint *touchPts = env->GetIntArrayElements(touchPoints, NULL);
	if (touchPts == NULL) {
		return 0;
	}
	//LOGD("kevin jni value = %d w=%d h=%d touchX = %d touchY= %d", level, w, h, touchX, touchY);

	Mat imgData(h, w, CV_8UC4, (unsigned char*) cbuf);
	int flags = 4 + (255 << 8);
	IplImage src_data;
	src_data = IplImage(imgData);

	IplImage *src = cvCloneImage(&src_data);

	Point seed = Point(touchPts[2*(touchPointsCount-1)],touchPts[2*(touchPointsCount-1)+1]);
	int g_nFillMode = 2;//漫水填充的模式,使用渐变、浮动范围的漫水填充
	int g_nLowDifference = 3+level/10, g_nUpDifference = 4;//负差最大值、正差最大值
	int g_nConnectivity = 4;//表示floodFill函数标识符低八位的连通值
	int g_nNewMaskVal = 255;//新的重新绘制的像素值
	int LowDifference = g_nFillMode == 0 ? 0 : g_nLowDifference;//空范围的漫水填充，此值设为0，否则设为全局的g_nLowDifference
	int UpDifference = g_nFillMode == 0 ? 0 : g_nUpDifference;//空范围的漫水填充，此值设为0，否则设为全局的g_nUpDifference
	Rect ccomp;

	IplImage* dst = cvCreateImage(cvGetSize(src), src->depth, 3);
	IplImage* color = cvCreateImage(cvGetSize(src), src->depth, 3);
	cvCvtColor(src,color,CV_BGRA2BGR);
	Mat dst_mat(color,0);//目标图的赋值
	int area;

	IplImage* show = cvCreateImage(cvGetSize(src), src->depth, src->nChannels);
	IplImage* out = cvCreateImage(cvGetSize(src), src->depth, src->nChannels);
	//IplImage* hsv = cvCreateImage(cvGetSize(src), src->depth, 3);
	CvSize size_src = cvGetSize(src);
	IplImage* maskImage = cvCreateImage(cvGetSize(src), src->depth,1);
	IplImage* mask_inv = cvCreateImage(cvSize(size_src.width, size_src.height),
			src->depth, 1);
	IplImage* gray = cvCreateImage(cvSize(size_src.width, size_src.height),
			src->depth, 1);
	IplImage* gray_dst = cvCreateImage(cvSize(size_src.width, size_src.height),
			src->depth, 1);
	IplImage* gray_mask = cvCreateImage(cvSize(size_src.width, size_src.height),
			src->depth, 1);
	IplImage* dilate_mask = cvCreateImage(cvSize(size_src.width, size_src.height),
			src->depth, 1);
	//IplImage* h_plane = cvCreateImage(cvSize(size_src.width, size_src.height),src->depth, 1);
	//IplImage* s = cvCreateImage(cvSize(size_src.width, size_src.height),src->depth, 1);
	//IplImage* v = cvCreateImage(cvSize(size_src.width, size_src.height),src->depth, 1);
	IplImage* r = cvCreateImage(cvSize(size_src.width, size_src.height),
			src->depth, 1);
	IplImage* g = cvCreateImage(cvSize(size_src.width, size_src.height),
			src->depth, 1);
	IplImage* b = cvCreateImage(cvSize(size_src.width, size_src.height),
			src->depth, 1);

	IplConvKernel* structure = cvCreateStructuringElementEx(7, 7, 3,3,CV_SHAPE_ELLIPSE);

	cvZero(dst);
	cvZero(maskImage);

	CvSeq* comp = NULL;

	CvMemStorage* storage = cvCreateMemStorage(0);

	CvSeq* contours = 0;

	//Mat g_maskImage(maskImage,0);

	Mat g_maskImage;
	g_maskImage.create(size_src.height+2, size_src.width+2, CV_8UC1);//利用image0的尺寸来初始化掩膜mask

	area = floodFill(dst_mat, g_maskImage, seed, cvScalar(255), &ccomp, Scalar(LowDifference, LowDifference, LowDifference),
		Scalar(UpDifference, UpDifference, UpDifference), flags);


	cvCvtColor(color, gray, CV_BGR2GRAY);

	//cvCvtColor(color, hsv, CV_BGR2HSV_FULL);//CV_RGB2HSV

	//cvSplit(hsv, h_plane, s, v, 0);

	//cvCvtColor(color, h_plane, CV_BGR2GRAY);

	//cvSet2D(maskImage,touchPts[2*(touchPointsCount-1)+1],touchPts[2*(touchPointsCount-1)],cvScalar(255));//传入坐标设在这里，这两个100,100
	IplImage temp = IplImage(g_maskImage);
	IplImage *mask_ff = &temp;
	cvSetImageROI(mask_ff,cvRect(2,2,size_src.width,size_src.height));
	cvCopy(mask_ff,maskImage);
	cvResetImageROI(mask_ff);
	Grow(gray,maskImage,level);//传入的level设在这里，

	cvFindContours(maskImage, storage, &contours, sizeof(CvContour),
			CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE); //CV_RETR_CCOMP,
	cvZero(maskImage);
	cvDrawContours(maskImage, contours, cvScalar(255), cvScalar(255), 0,
			CV_FILLED, 8);
	/*
	for (int i = 0; contours != 0; contours = contours->h_next) {
		cvDrawContours(maskImage, contours, cvScalar(255), cvScalar(255), 0,
				CV_FILLED, 8);
	}
	*/
	//cvDilate(maskImage,maskImage,structure,2);
	//cvErode(maskImage,maskImage,structure,2);


	if(bgBlur == 1)
	{
		cvDilate(maskImage,maskImage,structure,1);
		cvErode(maskImage,maskImage,structure,2);
	}
	else
	{

	}


	if(bgBlur == 1)
	{
		//cvErode(maskImage,maskImage,structure,2);
		cvCopy(src, show, maskImage);

		cvThreshold(maskImage, mask_inv, 1, 128, CV_THRESH_BINARY_INV);

		cvZero(dilate_mask);
		for(int i =0;i<3;i++)
		{
			cvCopy(maskImage,dilate_mask);
			cvDilate(dilate_mask,dilate_mask,structure,i*2);
			cvThreshold(dilate_mask, mask_inv, 1, 128, CV_THRESH_BINARY_INV);
			cvCopy(gray, gray_mask, mask_inv);
			cvSmooth(gray_mask,gray_mask,CV_BLUR,11+2*i,11+2*i,2,2);//CV_BLUR
		}

	}
	else
	{
		cvCopy(src, show, maskImage);

		cvThreshold(maskImage, mask_inv, 1, 128, CV_THRESH_BINARY_INV);

		cvZero(dilate_mask);
		cvCopy(gray, gray_mask, mask_inv);
	}

	Mat mat_r(r, 0);
	Mat mat_g(g, 0);
	Mat mat_b(b, 0);
	cvSplit(show, b, g, r, 0);

	Mat mat_gray_inv(gray_mask, 0);
	mat_r = mat_gray_inv + r;
	mat_g = mat_gray_inv + g;
	mat_b = mat_gray_inv + b;

//	gray =0,
//	GREEN=1,
//	BLUE =2,
//	YELLOW=3,
//	PURPLE=4,

	switch(bgColor){

		case 0:break;
		case 1:cvAddS(g,cvScalar(25),g);
			   break;
		case 2:cvAddS(b,cvScalar(25),b);
			   break;
		case 3:cvAddS(g,cvScalar(25),g);
			   cvAddS(r,cvScalar(25),r);
		   	   break;
		case 4:cvAddS(r,cvScalar(25),r);
	   	   	   break;
		default:break;

	}

	//cvAddS(r,cvScalar(25),r);

    cvMerge(b,g,r,0,show);

    //cvSmooth(show,show,CV_BLUR);//CV_BLUR


	uchar* ptr = imgData.ptr(0);

	for (int i = 0; i < h; i++)
	{
		for(int j = 0; j < w ; j++)
		{

		ptr[4 * (i*w+j) + 0] = cvGet2D(show,i,j).val[0];
		ptr[4 * (i*w+j) + 1] = cvGet2D(show,i,j).val[1];
		ptr[4 * (i*w+j) + 2] = cvGet2D(show,i,j).val[2];
		}
	}

	int size = w * h;
	jintArray result = env->NewIntArray(size);
	env->SetIntArrayRegion(result, 0, size, cbuf);
	env->ReleaseIntArrayElements(buf, cbuf, 0);

	cvReleaseImage(&src);
	cvReleaseImage(&dst);
	cvReleaseImage(&show);
	//cvReleaseImage(&hsv);
	cvReleaseImage(&maskImage);
	cvReleaseImage(&mask_inv);
	cvReleaseImage(&gray);
	cvReleaseImage(&gray_dst);
	cvReleaseImage(&gray_mask);
	//cvReleaseImage(&h_plane);
	cvReleaseImage(&color);
	//cvReleaseImage(&s);
	//cvReleaseImage(&v);
	cvReleaseImage(&dilate_mask);
	cvReleaseImage(&r);
	cvReleaseImage(&g);
	cvReleaseImage(&b);
	cvReleaseImage(&out);
	cvReleaseStructuringElement(&structure);
	return result;
}


Mat RegionGrow(Mat src, CvPoint pt, int th)
{
	CvPoint ptGrowing;
	CvPoint _pt_test;
    int nGrowLable = 0;
    int nSrcValue = 0;
    int nCurValue = 0;
    Mat matDst = Mat::zeros(src.size(), CV_8UC1);

    int DIR[8][2] = {{-1,-1}, {0,-1}, {1,-1}, {1,0}, {1,1}, {0,1}, {-1,1}, {-1,0}};
    Vector<CvPoint> vcGrowPt;
    vcGrowPt.push_back(pt);

    matDst.at<uchar>(pt.y, pt.x) = 255;
    nSrcValue = src.at<uchar>(pt.y, pt.x);

    while (!vcGrowPt.empty())
    {
        pt = vcGrowPt.back();
        vcGrowPt.pop_back();

        for (int i = 0; i<9; ++i)
        {
            ptGrowing.x = pt.x + DIR[i][0];
            ptGrowing.y = pt.y + DIR[i][1];

            if (ptGrowing.x < 0 || ptGrowing.y < 0 || ptGrowing.x > (src.cols-1) || (ptGrowing.y > src.rows -1))
                continue;

            nGrowLable = matDst.at<uchar>(ptGrowing.y, ptGrowing.x);

            if (nGrowLable == 0)
            {
                nCurValue = src.at<uchar>(ptGrowing.y, ptGrowing.x);
                vcGrowPt.push_back(ptGrowing);

                if (abs(nSrcValue - nCurValue) < th)
                {
                    matDst.at<uchar>(ptGrowing.y, ptGrowing.x) = 255;
                    _pt_test.x = 100;
                    _pt_test.y = 100;
                    vcGrowPt.push_back(_pt_test);
                }

            }

        }

    }

    return matDst.clone();
}


void Grow(IplImage* src,IplImage* seed, int t1)//
{
	Vector <CvPoint> seedd;
	CvPoint point;

	int height     = src->height;
	int width      = src->width;
	int step       = src->widthStep;
	uchar* seed_data    = (uchar *)seed->imageData;
	uchar* src_data=(uchar *)src->imageData;
	int temp;
	//将种子点压入堆栈
	for(int i=0;i<height;i++)
	{
		for(int j=0;j<width;j++)
		{
			if(seed_data[i*step+j]==255)
			{
				point.x=i;
				point.y=j;
				temp = src_data[point.x*step+point.y];
				seedd.push_back(point);
			}
		}
	}

	CvPoint temppoint;
	while(!seedd.empty())
	{
		point = seedd.back();
		seedd.pop_back();
		if((point.x>0)&&(point.x<(height-1))&&(point.y>0)&&(point.y<(width-1)))
		{
			if((seed_data[(point.x-1)*step+point.y]==0)&&(abs(src_data[(point.x-1)*step+point.y]-temp) <= t1))
			{
				seed_data[(point.x-1)*step+point.y]=255;
				temppoint.x=point.x-1;
				temppoint.y=point.y;
				seedd.push_back(temppoint);
			}
			if((seed_data[point.x*step+point.y+1]==0)&&(abs(src_data[point.x*step+point.y+1]-temp) <= t1))
			{
				seed_data[point.x*step+point.y+1]=255;
				temppoint.x=point.x;
				temppoint.y=point.y+1;
				seedd.push_back(temppoint);
			}
			if((seed_data[point.x*step+point.y-1]==0)&&(abs(src_data[point.x*step+point.y-1]-temp) <= t1))
			{
				seed_data[point.x*step+point.y-1]=255;
				temppoint.x=point.x;
				temppoint.y=point.y-1;
				seedd.push_back(temppoint);
			}
			if((seed_data[(point.x+1)*step+point.y]==0)&&(abs(src_data[(point.x+1)*step+point.y]-temp) <= t1))
			{
				seed_data[(point.x+1)*step+point.y]=255;
				temppoint.x=point.x+1;
				temppoint.y=point.y;
				seedd.push_back(temppoint);
			}
			if((seed_data[(point.x-1)*step+point.y-1]==0)&&(abs(src_data[(point.x-1)*step+point.y-1]-temp) <= t1))
			{
				seed_data[(point.x-1)*step+point.y-1]=255;
				temppoint.x=point.x-1;
				temppoint.y=point.y-1;
				seedd.push_back(temppoint);
			}
			if((seed_data[(point.x-1)*step+point.y+1]==0)&&(abs(src_data[(point.x-1)*step+point.y+1]-temp) <= t1))
			{
				seed_data[(point.x-1)*step+point.y+1]=255;
				temppoint.x=point.x-1;
				temppoint.y=point.y+1;
				seedd.push_back(temppoint);

			}
			if((seed_data[(point.x+1)*step+point.y-1]==0)&&(abs(src_data[(point.x+1)*step+point.y-1]-temp) <= t1))
			{
				seed_data[(point.x+1)*step+point.y-1]=255;
				temppoint.x=point.x+1;
				temppoint.y=point.y-1;
				seedd.push_back(temppoint);
			}
			if((seed_data[(point.x+1)*step+point.y+1]==0)&&(abs(src_data[(point.x+1)*step+point.y+1]-temp) <= t1))
			{
				seed_data[(point.x+1)*step+point.y+1]=255;
				temppoint.x=point.x+1;
				temppoint.y=point.y+1;
				seedd.push_back(temppoint);
			}
		}
	}






}

}




