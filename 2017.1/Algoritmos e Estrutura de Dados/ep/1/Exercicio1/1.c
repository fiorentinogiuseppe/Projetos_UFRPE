#include <stdio.h>
#include <stdlib.h>
//1
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
}
