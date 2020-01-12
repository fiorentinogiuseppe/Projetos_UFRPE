#include<stdio.h>
#include<stdlib.h>
////2
int contadorIntera=0,contadorRecu=0;
int ordemrecu(int e,int n,int v[]){////wroooggg
    contadorRecu++;
    int m, d=n;
    if(e==d-1){
        if(v[e]<=v[d])  return 1;
        else return 0; }
    m=(e+d)/2;
    int x=ordemrecu(e,m,v);
    int y=ordemrecu(m,d,v);
    if(x==1 && y==1) return 1;
    else return 0;
}
int ordemitera(int n,int v[]){
    int contador=0;
    for(int i=1;i<n;i++){
        contadorIntera++;
        if(v[i]<v[i-1]) contador++;
    }
    if(contador==0) return 1;
    else return 0;
}
void arquivo(){
    FILE *arq;
    arq=fopen("dados2nd.xlsx","r");
    if(arq==NULL){
        arq=fopen("dados2nd.xlsx","w");
	fprintf(arq,"Iterativo;Recursivo;\n%i;%i",contadorIntera,contadorRecu);
    }
    else{
	arq=fopen("dados2nd.xlsx","a");
    	fprintf(arq,"\n%i;%i",contadorIntera,contadorRecu);
    }

}
int main()
{
    int n=5;
    int e=0;
    //int v[]={2000,1000,999,888,777,666,555,444,333,222,111};
    //int v[]={111,222,333,444,555,666,777,888,999,1000,2000};


    int v[]={-5,-4,-3,-2,-1};
    int respint=ordemitera (n, v);
    int resprecu=ordemrecu(e,n,v);
    printf("Ordem Iterativamente. Esta em ordem?[0=F/1=V]: %i\n",respint);
    printf("Ordem Recursivamente. Esta em ordem?[0=F/1=V]: %i\n",resprecu);
    arquivo();
    return 0;
}
