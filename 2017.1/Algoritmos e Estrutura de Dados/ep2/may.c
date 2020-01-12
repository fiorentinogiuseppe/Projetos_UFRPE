/*Aluna: Mayara Simões de Oliveira Castro
Data: 30/06/2017        EP 2
Disciplina: Algoritmos e Estrutura de Dados
 */

#include <stdio.h>
#include <stdlib.h>
//#include <conio.h>
#include <math.h>

//Variaveis para obter os indices dos dois pontos de menor distancia entre si
int i1,i2;
//Variavel que guarda a menor distancia encontrada geral
float distGeral=2147483647.0;
#define min(a,b) (((a)<(b))?(a):(b))

/*
Esta função ordena a[] indiretamente em funçao de y[] de forma recursiva.

p int Começo do vetor.
r int Fim do vetor.
a[] int Vetor de inteiros.
y[] int vetor de inteiros( coordenadas y)
return void
 */
void mergeSortInd(int p, int r, int y[],int a[]) {

   if (p < r - 1) {

      int q = (p + r) / 2;

      mergeSortInd(p, q, y,a);
      mergeSortInd(q, r, y,a);
      intercala(p, q, r, y,a);
   }


}

/*Função auxiliar do mergeSort.
Esta função ordena um vetor v, que possui dois subvetores que vão de v[p...q - 1] e
v[q...r], de forma crescente.
p int Índice do início do vetor v.
q int Índice da metade do vetor v.
r int Índice do fim do vetor v.
v[] int Vetor de inteiros.
retorno void
 */
void intercala(int p, int q, int r, int x[],int y[]) {

   int i, j, k, *w,*v;
   w = malloc((r-p) * sizeof (int));
   v = malloc((r-p) * sizeof (int));

   i = p; j = q;
   k = 0;

   while (i < q && j < r) {

      if (x[i] <= x[j])
      { v[k]=y[i];
        w[k++] = x[i++];
      }
      else{
        v[k]=y[j]; w[k++] = x[j++];
      }
   }
   while (i < q) {
        v[k]=y[i];
      w[k++] = x[i++];

   }
   while (j < r) {

        v[k]=y[j];
        w[k++] = x[j++];

   }
   for (i = p; i < r; ++i) {

    y[i]=v[i-p];
    x[i] = w[i-p];
   }
   free (w);
   free (v);
}

/*
Esta função ordena crescentemente um vetor de forma recursiva
que começa em p e termina em r.

p int Começo do vetor.
r int Fim do vetor.
v[] int Vetor de inteiros.
return void
 */
void mergeSort(int p, int r, int x[],int y[]) {

   if (p < r - 1) {
      int q = (p + r) / 2;
      mergeSort(p, q, x,y);
      mergeSort(q, r, x,y);
      intercala(p, q, r, x,y);
   }

}

/*
Esta função calcula a distancia entre dois pontos (x1,y1) e (x2,y2)
recebe como parametro as coordenadas x e y de cada ponto
return float
 */
float distancia(int x1,int y1,int x2,int y2){
    float dist=sqrt(pow((x1-x2),2)+pow((y1-y2),2));

    return(dist);
}

/*
Esta função o identifica os pontos que estão na faixa, ordenados pela Y -coordenada
recebe:
x[] um vetor de inteiros representando as coordenadas x de cada ponto
a[] um vetor de inteiros
p int Índice do início dos vetores x e y.
r int Índice do fim dos vetores x e y.
d float  menor distancia entre dois pontos entre o lado direito ou o lado esquerdo
*t int um ponteiro que indica a quantidade de pontos encontrados na faixa
return int (endereco de um vetor int f)
 */
int candidatos(int x[], int a[], int p, int r, float d, int *t){
    int q =(p+r)/2;
    int *f = malloc(1*sizeof(int));
    int k, mod;
    *t=0;
    for(k=p;k<=r;k++){

        mod=(x[a[k]]-x[q]);

        if(mod<0)mod*=-1;

        if(mod<d){
            *t=*t+1;
            f = (int*)(realloc(f,(*t) * sizeof(int)));
            f[((*t) -1)]=a[k];
        }
    }
    return(f);
}
/*
Esta função Devolve o mínimo entre e,d e a menor distancia entre um ponto da esquerda e um pontoda direita.
recebe:
x[] e y[] vetores de inteiros representando as coordenadas x e y de cada ponto
a[] um vetor de inteiros
p int Índice do início dos vetores x e y.
r int Índice do fim dos vetores x e y.
e float menor distancia entre dois pontos do lado esquerdo
d float menor distancia entre dois pontos do lado direito
return float
 */
float combine(int x[], int y[], int a[], int p, int r, float e, float d){

    float dist = min(e,d);
    int* t = (int*)(malloc(1 * sizeof (int)));
    int i,j;
    int *f = candidatos(x, a, p, r, d, t);
    float d1;

    for(i=0; i< *t; i++){
        for( j=i+1; j < min(i+7,*t); j++){
            d1=distancia(x[f[i]], y[f[i]], x[f[j]], y[f[j]]);

            if(d1<dist){
                dist=d1;
                if(dist<distGeral){
                    distGeral=dist;
                    i1=f[i];
                    i2=f[j];
                }

            }
        }
    }
    return(dist);
}
/*
Esta função determina, recursivamente, a menor distância entre dois pontos.
recebe:
x[] e y[] vetores de inteiros representando as coordenadas x e y de cada ponto
a[] um vetor de inteiros
p int Índice do início dos vetores x e y.
r int Índice do fim dos vetores x e y.
return float
 */
float distanciaRecSH(int x[], int y[], int a[], int p, int r){
    if(r <= p + 1){
        float dist=distancia(x[p],y[p],x[r],y[r]);
        if(dist<distGeral){
            distGeral=dist;
            i1=r;
            i2=p;
        }
        return(dist);

    }
    else{
        int q = (p+r)/2;
        int b[r];
        divida(b, a, p, r, x, y);
        float e = distanciaRecSH(x,y,b,p,q);
        float d = distanciaRecSH(x,y,b,q,r);

        return(combine(x,y,a,p,r,e,d));
    }
}

/*
Esta função faz um pre-processamento ordenando os pontos pela X-coordenada
e ordenando indiretamente os pontos pela Y-coordenada
Após, é chamado a função distanciaRecSH e retorna a menor distancia entre dois pontos da coleção
recebe como parametro o vetor das coordenadas X, o vetor das coordenadas Y e a quantidade n de pontos
return float
 */
float distanciaSH(int x[], int y[], int n){
    mergeSort(0, n, x, y);
    int i;
    int a[n];
    for(i=0; i<n; i++){
        a[i] = i;
    }
    int copiaY[n];
    copiaVetor(n, y, copiaY);
    mergeSortInd(0, n, copiaY, a);
    return(distanciaRecSH(x,y,a,0,n-1));
}

/*
Esta função obtem um vetor int b[] de forma que b[p ..q] seja uma representação
ordenada dos pontos mais à esquerda e b[q+1 ..r], uma representação
ordenada dos pontos mais à direita
recebe:
x[] e y[] vetores de inteiros representando as coordenadas x e y de cada ponto
a[] um vetor de inteiros
p int Índice do início dos vetores x e y.
r int Índice do fim dos vetores x e y.
return void
 */
void divida(int* b,int a[],int p,int r , int x[], int y[]){
     int q=(p+r)/2;
     int i=p-1;
     int j=q;
     int k;
     for(k=p; k<=r;k++){
          if(a[k]<=q){
           i++;
           b[i]=a[k];
      }
      else{
           j++;
           b[j]=a[k];
      }
     }
}
/*
Copia os valores do vetor v para o vetor y
recebe:
v[] e y[] vetores do tipo inteiro;
n int Tamnho dos vetores
return void
*/
void copiaVetor(int n, int v[], int y[]){
    int i;
    for(i=0; i<n; i++){

        y[i] = v[i];
    }
}

/*
Esta função lê o arquivo .dat que contém o número de cidades e os pontos de cada cidade
recebe o ponteiro do arquivo
x e y como ponteiros do tipo int que guardarão as coordenadas x e y, respectivamente;
return int ( numero de cidades)
*/
int arquivo(FILE* arq,int* x, int* y){
    int n;
    fscanf(arq,"%i ",&n);

    y=realloc(y,n * sizeof(int));
    x=realloc(x,n * sizeof(int));
    int i;

    for(i=0;i<n;i++){
        fscanf(arq,"%i",&x[i]);

    }
    for(i=0;i<n;i++){
        fscanf(arq,"%i",&y[i]);
    }
    fclose(arq);
    return(n);
}
int main()
{
    FILE *arq;
    int result;
    int i;

    arq = fopen("trembala.dat", "rb");

    int* x = calloc(x,1*sizeof(int));
    int* y = calloc(y,1*sizeof(int));
    int n = arquivo(arq,x,y);

    printf("%.3f",distanciaSH(x,y,n));
    printf(" %i %i",i1,i2);
    return 0;
}
