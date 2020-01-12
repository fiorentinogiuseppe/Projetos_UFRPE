#include<stdio.h>
#include<stdlib.h>
int contadorIntera=0,contadorRecu=0;

int potenciaRecu(int base,int e,int d){
    contadorRecu++;

    if( e>d-1 || e==d-1 ) return base;

    int m=(d+e)/2;

    int x=potenciaRecu(base,e,m);
    int y=potenciaRecu(base,m,d);

    int valor=x*y;
    return valor;

}
int potenciaIte(int b,int e){

    int i,produto=1;
    for(i=0;i<e;i++){
        contadorIntera++;
        produto*=b;
    }
    return produto;

}
void arquivo(){
    FILE *arq;
    arq=fopen("dados3rd.xlsx","r");
    if(arq==NULL){
        arq=fopen("dados3rd.xlsx","w");
	fprintf(arq,"Iterativo;Recursivo;\n%i;%i",contadorIntera,contadorRecu);
    }
    else{
	arq=fopen("dados3rd.xlsx","a");
    	fprintf(arq,"\n%i;%i",contadorIntera,contadorRecu);
    }
}
int main()
{
    int base=2;
    int f=10;
    int i=0;
    int resprecu=potenciaRecu(base,i,f);
    int respite=potenciaIte(base,f);
    printf("Potencia Recursiva: %i\n",resprecu);
    printf("Potencia Iterativa: %i\n",respite);
    arquivo();
    return 0;
}

