#include<stdio.h>
#include<stdlib.h>
#include<time.h>
long int contadorRecu=0, contadorIntera=0;
void insercao (int n, int v[]){//iterativo
   int i, j, x;
   for (j = 1; j < n; ++j) {
      x = v[j];
      for (i = j-1; i >= 0 && v[i] > x; --i){ 
		contadorIntera++;         
		v[i+1] = v[i];
	}
      v[i+1] = x;
   }
}
int insertion(int ini, int n, int v[]){//Recu
	contadorRecu++;
	if(ini>=n-1) return n;
	int m=(ini+n)/2;
	int x=insertion(ini, m, v);
	int y=insertion(m+1, n, v);
	int cte=y;
	while(cte>x){
		if(v[cte]<v[x]){
			int w=v[cte];
			v[cte]=v[x];
			v[x]=w;		
		}
		--cte;	
	}

	return y;
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


void main(void)
{
	
	int n=100000;
	srand(time(NULL));  
	int v[n];
	int v1[n];
	for(int i=0;i<n;i++) {
		v[i]=rand();
		v1[i]=v[i];
	}
	int ini=-1;
	insertion(ini,n,v);
	insercao (n, v1);
	arquivo(n);
	
}
