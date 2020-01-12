#include <stdio.h>
#include <stdlib.h>
/*
4
int buscaBinaria (int x, int n, int v[]) {//iteratividade
   int e, m, d;
   e = -1; d = n;  // atenção!
   while (e < d-1) {
      m = (e + d)/2;
      printf("%i\n",e);
      printf ("%i\n",d);
      printf("%i\n",m);
      if (v[m] < x) e = m;
      else d = m;
   }
   return d;
}
int bb(int x, int n,int e, int v[]){//recursividade
    int d=n;
    int m=(e+d)/2;
    int reg_index=n-1;
    int n1=reg_index-m;
    int k;
    if(e==d-1) return d;

    if(v[m]<x){
         return k=bb( x,  n, m,  v);

    }
    else{
        return k=bb( x,  m, e,  v);
    }

}


int main()
{
    int n=12;
    int e=-1;
    //int v[]=  {10,50,50,70,80,80,100,120,140,140,150,200};
    //int v[]={50,50,70,80,90,130,140,140,150,150,150,160};
    int v[]={000,111,222,333,444,555,666,777,888,999,1000,2000};
    int x=110;
    //int respint=buscaBinaria (x, n, v);
    int resprecu=bb(x,n,e,v);
    //printf("Busca Binaria Iterativa: %i",respint);
    printf("Busca Binaria Recursiva: %i",resprecu);
    return 0;
}
/////////////3

int potenciarecu(int base,int e,int d){

    if( e>d-1 || e==d-1 ) return base;

    int m=(d+e)/2;

    int x=potenciarecu(base,e,m);
    int y=potenciarecu(base,m,d);

    int valor=x*y;
    return valor;

}
int potenciaite(int b,int e){

    int i,produto=1;
    for(i=0;i<e;i++){
        produto*=b;
    }
    return produto;

}
int main()
{
    int base=2;
    int f=10;
    int i=0;
    int resprecu=potenciarecu(base,i,f);
    //int respite=potenciaite(b,e);
    printf("Potencia: %i",resprecu);
    //printf("Potencia: %li",respite);
    return 0;
}


////1
int maiorrecu(int e,int n,int v[]){
    int m, d;
    d = n;  // atenção!
    if( e==d-1 ) return  v[e];
    m=(e+d)/2;

    int x=maiorrecu(e,m,v);
    int y=maiorrecu(m,d,v);

    if(x>y) return x;
    else return y;

}
int main()
{
    int n=12;
    int e=-1;
    //int v[]=  {10,50,50,70,80,80,100,120,140,140,150,200};
    //int v[]={50,50,70,80,90,130,140,140,150,150,150,160};
    int v[]={000,111,222,333,444,555,666,777,888,999,1000,2000};
    //int respint=buscaBinaria (x, n, v);
    int resprecu=maiorrecu(e,n,v);
    //printf("Busca Binaria Iterativa: %i",respint);
    printf("Busca Binaria Recursiva: %i",resprecu);
    return 0;
}*/

////2
int ordemrecu(int e,int n,int v[]){////wroooggg
    int m, d=n;
    if( e==d+1){
        if(v[e]<=v[d+1])  return 1;
        else return 0; }
    m=(e+d)/2;
    int x=ordemrecu(e,m,v);
    int y=ordemrecu(m,d,v);
    if(x==1 && y==1) return 1;
    else return 0;
}

int main()
{
    int n=5;
    int e=0;
    //int v[]={2000,1000,999,888,777,666,555,444,333,222,111};

   int v[]={-1,0,0,0};
    //int respint=buscaBinaria (x, n, v);
    int resprecu=ordemrecu(e,n,v);
    //printf("Busca Binaria Iterativa: %i",respint);
    printf("Esta em ordem crescente?: %i",resprecu);
    return 0;
}
