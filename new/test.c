#include <stdio.h>
#include  <math.h>
int main(){
   int data_len = 512*512*128;
   int filter_len = 512;
   long unsigned int len=((long unsigned int)data_len) * ((long unsigned int)filter_len);
   int m=0, n=0;
   long unsigned int j = 0;
   printf("len= %lu\n",len);
   while(j<len){
	m = floor(j/data_len);
	n = j % data_len;
	j++;
   }
   printf("len = %lu, m= %d, n=%d \n",len,m,n);
}
