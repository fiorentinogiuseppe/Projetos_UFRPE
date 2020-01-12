#include<stdio.h>
#include <time.h>
#include <stdlib.h>
long int contadorRecu=0, contadorIntera=0;
int Buscamenor (int ini, int n, int v[]) {
   	if(ini==n) return n;
	int m=(ini+n)/2;
	int x=Buscamenor(ini, m, v);
	int y=Buscamenor(m+1, n, v);

	if(v[x]>v[y]){

		return y;
	}
	else{

		return x;
	}
}

void SelecaoRecursiva(int ini,int n, int v[]){
	for(int i=ini;i<n;i++) {
		contadorRecu++;
		int menor1=i;
		int menor=Buscamenor(i+1,n,v); 

		if(v[menor1]>v[menor]) {
			int temp=v[menor];
			v[menor]=v[menor1];	
			v[menor1]=temp;
		}
	}
}

void selecaoIterativa (int n, int v[])
{
   int i, j, min, x;
   for (i = 0; i < n-1; ++i) {
      min = i;
      for (j = i+1; j < n; ++j){
	 contadorIntera++;
         if (v[j] < v[min])  min = j;
	}
      x = v[i]; v[i] = v[min]; v[min] = x;
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

void main(void){
	srand(time(NULL)); 
	int n=100000;
	srand(time(NULL));  
	int v[n];
	int v1[n];
	int ini=0;
	for(int i=ini;i<n;i++) {v[i]=rand(); v1[i]=v[i];}
	SelecaoRecursiva(ini,n-1,v);
	selecaoIterativa(n,v1);
	arquivo(n);

}
