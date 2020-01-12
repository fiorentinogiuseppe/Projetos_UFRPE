#include<stdio.h>
#include<time.h>
#include<stdlib.h>
int contadorIntera=0;
int contadorRecu=0;

void
merge (int p, int q, int r, int v[])
{
   int i, j, k, *w;
   w = malloc ((r-p) * sizeof (int));

   for (i = p; i < q; ++i)  w[i-p] = v[i];
   for (j = q; j < r; ++j)  w[r-p+q-j-1] = v[j];
   i = 0; j = r-p-1;
   for (k = p; k < r; ++k)
      if (w[i] <= w[j]) v[k] = w[i++];
      else v[k] = w[j--];
   free (w);
}


void mergesort(int ini, int n, int v[]){
	contadorRecu++;
	if(ini>=n-1) return;
	int m=(ini+n)/2;
	mergesort(ini,m,v);
	mergesort(m+1,n,v);
	merge(ini,m,n,v);
}
void mergesort_i (int n, int v[])
{
   int p, r;
   int b = 1;
   while (b < n) {
      p = 0;
      while (p + b < n) {	
	contadorIntera++;
         r = p + 2*b;
         if (r > n) r = n;
         merge (p, p+b, r, v);
         p = p + 2*b; 
      }
      b = 2*b;
   }
}

void main(void){
	
	int n=1000000;
	srand(time(NULL));  
	int v[n];
	int v1[n];
	for(int i=0;i<n;i++) {
		v[i]=rand();
		v1[i]=v[i];
	}
	int ini=0;
	mergesort(ini,n-1,v);
	mergesort_i(n,v);
	arquivo(n);

}
