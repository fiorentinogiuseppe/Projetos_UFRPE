#include<stdio.h>
#include<stdlib.h>
void peneira(int i, int m, int v[]){
   int p = i, f = i+1, t = v[i];
   while (f <= m) {
      if (f < m && v[f] < v[f+1])  ++f;
      if (t >= v[f]) break;
      v[p] = v[f];
      p = f, f = 2*p;
   }
   v[p] = t;

}
void printarvet(int i, int n,int v[]){
	int inicio;
	for(inicio=i;inicio<=n;inicio++) printf("%i",v[i]);

}
void heap(int p,int m, int v){
        for (p = 1; p <= m/2; ++p){
                peneira (p, m, v);
		printarvet(p,m,v);
	}

}
void main(void){
        int v[]={0,1,2,3,4,5,6};
        heap(1,6,v);
}
