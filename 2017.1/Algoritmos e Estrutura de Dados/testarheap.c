#include<stdio.h>
int testarHeap(int n, int v[]){

	for(int f=n;f>1;f--){

		if(v[f/2]<v[f] && (f/2)>1){
			return 0;
		}	
	}
	return 1;
}
void main(void)
{
	int n=5;

	int v[]={0,9,1,5,7,3};

	int resp=testarHeap(n,v);
	printf("Resp=%i",resp);
	
}
