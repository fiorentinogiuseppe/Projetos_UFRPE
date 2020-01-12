#include<stdio.h>

int loga(int ini, int X , int n){

	int XX=X;
	int XY=X;
	int q;
	if(ini>=n) return X;
	q=(ini+n)/2;
	int x=loga(ini,++XX,q);
	int y=loga(q+1,++XY,n);
	if(x<y) return x;
	else return y;

}
void main(void){
	int ini=0;
	int n=4;
	int X=0;
	printf("%i",loga(ini,X,n-1));


}
