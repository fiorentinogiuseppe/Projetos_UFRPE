#include <stdio.h>
#include<stdlib.h>

int contadorRecu=0,contadorIntera=0;

int maiorRecu(int e,int n,int v[]){
    contadorRecu++;
    int m, d = n;
    if( e==d-1 ) return  v[e];
    m=(e+d)/2;

    int x=maiorRecu(e,m,v);
    int y=maiorRecu(m,d,v);

    if(x>y) return x;
    else return y;

}

int maiorItera(int n,int v[]){
    int maior=v[0];
    for(int i=1;i<n;i++){
        contadorIntera++;
        if(v[i]>maior)  maior=v[i];
    }
    return maior;
}
void arquivo(int n){
    FILE *arq;
    arq=fopen("dados1st.xlsx","r");
    if(arq==NULL){
        arq=fopen("dados1st.xlsx","w");
	fprintf(arq,"Iterativo;Recursivo;ElementosTotais\n%i;%i;%i",contadorIntera,contadorRecu,n);
    }
    else{
	arq=fopen("dados1st.xlsx","a");
    	fprintf(arq,"\n%i;%i;%i",contadorIntera,contadorRecu,n);
    }
}
int main()
{
    int n=10;
    int e=-1;
    int v[]={1,50,50,70,80,80,100,120,140,140,150,200,250,300};


    int respIte=maiorItera (n, v);
    printf("O maior eh: %i\n",respIte);

    int respRecu=maiorRecu(e,n,v);
    printf("O maior eh: %i\n",respRecu);

    arquivo(n);

    return 0;
}

