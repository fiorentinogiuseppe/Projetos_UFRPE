#include<stdio.h>
#include<stdlib.h>
int contadorIntera=0,contadorRecu=0;
//algoritmo iterativo
int buscaItera (int x, int n, int v[]) {
   int e= -1, m, d= n;
   while (e < d-1) {
      contadorIntera++;
      m = (e + d)/2;
      if (v[m] < x) e = m;
      else d = m;
   }
   return d;
}
//algoritmo recursivo
int buscaRecu(int x, int n,int e, int v[]){
    contadorRecu++;
    int d=n,k,m=(e+d)/2;
    if(e==d-1) return d;

    if(v[m]<x) return k=buscaRecu( x,  n, m,  v);
    else return k=buscaRecu( x,  m, e,  v);
}
void arquivo(){
    FILE *arq;
    arq=fopen("dados4th.xlsx","r");
    if(arq==NULL){
        arq=fopen("dados4th.xlsx","w");
	fprintf(arq,"Iterativo;Recursivo;\n%i;%i",contadorIntera,contadorRecu);
    }
    else{
	arq=fopen("dados4th.xlsx","a");
    	fprintf(arq,"\n%i;%i",contadorIntera,contadorRecu);
    }

}

int main(){

    int n=12;
    int e=-1;
    int v[]={000,111,222,333,444,555,666,777,888,999,1000,2000};
    int x=100;

    int respint=buscaItera (x, n, v);
    printf("Busca Binaria Iterativa: %i\n",respint);

    int resprecu=buscaRecu(x,n,e,v);
    printf("Busca Binaria Recursiva: %i\n",resprecu);
    arquivo();
    return 0;
}
