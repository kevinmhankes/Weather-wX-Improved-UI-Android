/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

    This file is part of wX.

    wX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    wX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with wX.  If not, see <http://www.gnu.org/licenses/>.

 */

#include "genTriangle.h"

JNIEXPORT void JNICALL Java_joshuatee_wx_Jni_genTriangle(JNIEnv * env, jclass clazz, jobject loc_buff, jobject index_buff, jfloat center_x, jfloat center_y, jfloat x_image_center_pixels, jfloat y_image_center_pixels, jfloat one_degree_scale_factor, jdoubleArray x, jdoubleArray y, jint count, jfloat len, jobject color_buff, jbooleanArray col) {
	jfloat* lBuff = (*env)->GetDirectBufferAddress(env, loc_buff);
	jshort* iBuff = (*env)->GetDirectBufferAddress(env, index_buff);
	jbyte* cBuff = (*env)->GetDirectBufferAddress(env, color_buff);
	jdouble* x_arr = (*env)->GetDoubleArrayElements(env, x, 0);
	jdouble* y_arr = (*env)->GetDoubleArrayElements(env, y, 0);
	jbyte* col_arr = (*env)->GetByteArrayElements(env, col, 0);
	double point_x;
	double point_y;
	float pix_y_d;
	float pix_x_d;
	int l_count = 0;
	int c_count = 0;
	int ix_count = 0;
	float test1;
	float test2;
	double W_180_DIV_PI = 180.0 / M_PI;
	double W_PI_DIV_360 = M_PI / 360.0 ;
	double W_PI_DIV_4 = M_PI / 4.0;
	for (int i_count = 0; i_count < count; i_count++) {
		point_x = x_arr[i_count];
		point_y = y_arr[i_count];
		test1 = W_180_DIV_PI * log(tan(W_PI_DIV_4+point_x*W_PI_DIV_360));
		test2 = W_180_DIV_PI * log(tan(W_PI_DIV_4+center_x*W_PI_DIV_360));
		pix_y_d = -((test1 - test2) *  one_degree_scale_factor ) + y_image_center_pixels;
		pix_x_d = -((point_y - center_y ) * one_degree_scale_factor ) + x_image_center_pixels;
		lBuff[l_count] = pix_x_d;
		lBuff[l_count + 1] = -pix_y_d;
		lBuff[l_count + 2] = pix_x_d - len;
		lBuff[l_count + 3] = -pix_y_d + len;
		lBuff[l_count + 4] = pix_x_d + len;
		lBuff[l_count + 5] = -pix_y_d + len;
		l_count += 6;
		iBuff[ix_count] = ix_count;
		iBuff[ix_count + 1] = ix_count + 1;
		iBuff[ix_count + 2] = ix_count + 2;
		ix_count += 3;
		cBuff[c_count] = col_arr[0];
		cBuff[c_count + 1] = col_arr[1];
		cBuff[c_count + 2] = col_arr[2];
		cBuff[c_count + 3] = col_arr[0];
		cBuff[c_count + 4] = col_arr[1];
		cBuff[c_count + 5] = col_arr[2];
		cBuff[c_count + 6] = col_arr[0];
		cBuff[c_count + 7] = col_arr[1];
		cBuff[c_count + 8] = col_arr[2];
		c_count += 9;
	}
}
