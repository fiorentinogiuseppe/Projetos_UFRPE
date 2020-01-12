#include<stdio.h>
#include<time.h>
#include<stdlib.h>
long int contadorRecu=0, contadorIntera=0;
static int separa (int v[], int p, int r)
{
   int c = v[p], i = p+1, j = r, t;
   while (/*A*/ i <= j) {
     contadorRecu++;
      if (v[i] <= c) ++i;
      else if (c < v[j]) --j; 
      else {
         t = v[i], v[i] = v[j], v[j] = t;
         ++i; --j;
      }
   }
   // agora i == j+1                 
   v[p] = v[j], v[j] = c;
   return j; 
}
void quicksort (int v[], int p, int r)
{
   int j;                         // 1
   if (p < r) {                   // 2
      j = separa (v, p, r);       // 3
      quicksort (v, p, j-1);      // 4
      quicksort (v, j+1, r);      // 5
   }
}
void quicksortIterativo (int v[], int p, int r)//iterativo
{
   int j, *pilhap, *pilhar, t;

   pilhap = malloc ((r-p+1) * sizeof (int));
   pilhar = malloc ((r-p+1) * sizeof (int));
   pilhap[0] = p; pilhar[0] = r; t = 0; 
   
   while (t >= 0) { 
	contadorIntera++;    
      p = pilhap[t]; r = pilhar[t]; --t;
      if (p < r) {
         j = separa (v, p, r);    
         ++t; pilhap[t] = p; pilhar[t] = j-1; 
         ++t; pilhap[t] = j+1; pilhar[t] = r; 
      }
   }
}
void arquivo(int n){

    int diferenca=contadorIntera-contadorRecu;
    if(diferenca<0) diferenca=diferenca*(-1);

    FILE *arq;
    arq=fopen("questao","r");
    if(arq==NULL){
        arq=fopen("questao","w");
	fprintf(arq,"#Iterativo-#Recursivo-Diferenca-#Tamanho;\n%li,%li,%li,%li",contadorIntera,contadorRecu,diferenca,n);
    }
    else{
	arq=fopen("questao","a");
    	fprintf(arq,"\n%li,%li,%li,%li",contadorIntera,contadorRecu,diferenca,n);
    }
	printf("DONE\n");

}

void main( void){
	
	int n=5000000;
	srand(time(NULL));  
	int v[n];
	int v1[n];
	for(int i=0;i<n;i++) {
		v[i]=rand();
		v1[i]=v[i];
	}
	int ini=0;
	quicksort(v,ini,n-1); 
quicksortIterativo(v1,ini,n-1);

arquivo(n);

}
