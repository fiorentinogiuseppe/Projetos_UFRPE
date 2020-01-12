
#include<stdio.h>
#include<stdlib.h>
#include<math.h>
#include<float.h>


float MIN(float a,float b) {
    if(a<=b)
        return a;
    else
        return b;
}


int mdist1,mdist2;

float distGeral=FLT_MAX;


typedef struct{
	int x;
	int y;
} coordenada;


static void intercala (int p, int q, int r, coordenada v[])
{
   int i, j, k, *w;
   w = malloc ((r-p) * sizeof (int));

   for (i = p; i < q; ++i)  w[i-p] = v[i].x;
   for (j = q; j < r; ++j)  w[r-p+q-j-1] = v[j].x;
   i = 0; j = r-p-1;
   for (k = p; k < r; ++k)
      if (w[i] <= w[j]) v[k].x = w[i++];
      else v[k].x = w[j--];
   free (w);
}

void mergesort (int p, int r, coordenada v[])
{
   if (p < r-1) {
      int q = (p + r)/2;
      mergesort (p, q, v);
      mergesort (q, r, v);
      intercala (p, q, r, v);
   }

}

static void intercalaInd (int p, int q, int r, int v[],int a[])
{
   int i, j, k, *w;
   w = malloc ((r-p) * sizeof (int));

   for (i = p; i < q; ++i)  w[i-p] = v[i];
   for (j = q; j < r; ++j)  w[r-p+q-j-1] = v[j];
   i = 0; j = r-p-1;
   for (k = p; k < r; ++k)
      if (w[i] <= w[j]) {
	a[k]=i;
	v[k]= w[i++];
	}
      else {
	a[k]=j;
	v[k] = w[j--];
	}
   free (w);
}

void mergesortInd (int p, int r, int v[],int a[])
{
   if (p < r-1) {
      int q = (p + r)/2;
      mergesortInd (p, q, v,a);
      mergesortInd (q, r, v,a);
      intercalaInd (p, q, r, v,a);
   }
}

int candidatos(coordenada v[],int a[],int p,int r,int d, int * t){
    int q=(p+r)/2;
    int *f = malloc(1*sizeof(int));
    int k,dif;
    *t=0;

    for(k=p;k<=r;k++){
        dif=(v[a[k]].x-v[q].x);
        if(dif<0)dif*=-1;
        if(dif<d){
            *t=*t+1;
            f = (int*)realloc(f,(*t) * sizeof (int));
            f[((*t)-1)]=a[k];
        }
    }
    return f;
}

float distancia(int x1,int y1,int x2,int y2){
	float dist = sqrt(pow((x1-x2),2)+pow((y1-y2),2));
	return dist;
}

float combine(int a[], int e, int n,float dE, float dD, coordenada v[]){
    float dist= MIN(dE,dD);
    int *t=(int*)(malloc(1 * sizeof (int)));

    int *f=candidatos(v,a,e,n,dD,t);
    int i,j;
    float d1;

    for(i=1;i<*t;i++){
        for(j=i+1;j<MIN(i+7,*t);j++){
            d1=distancia(v[i].x,v[i].y,v[j].x,v[j].y);
            if(d1<dist){
                dist=d1;
		if(dist<distGeral){
                    distGeral=dist;
                    mdist1=f[i];
                    mdist2=f[j];
                }
            }
        }
    }

    return(dist);
}

float DistanciaRecSH(int a[], int e,int n,coordenada v[]){

    int m, d = n;

    if( d<=e+1 ) {

	float dist=distancia(v[e].x,v[e].y,v[d].x,v[d].y);

        if(dist<distGeral){
            distGeral=dist;
            mdist1=d;
            mdist2=e;
        }
        return(dist);
	}

    m=(e+d)/2;
    int b[n];
    Divida(b,a, e, n , v);
    float dE=DistanciaRecSH(b,e,m,v);
    float dD=DistanciaRecSH(b,m,d,v);
    return combine(a,e,n,dE,dD,v);
}


void Divida(int* b,int a[], int p, int r , coordenada v[]){
	int q=(p+r)/2;
	int i=p-1;
	int j=q,k;

	for(k=p; k<r;k++){
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

float distanciaSH(int n, coordenada v[]){
	int a[n],Y[n];
	int ini=0;
	mergesort(ini,n,v);


int i;
	for(i=ini;i<n;i++){
		a[i]=i;
	}
	for(i=ini;i<n;i++){
		Y[i]=v[i].y;
	}

	mergesortInd(ini,n,Y,a);

	return DistanciaRecSH(a,1,n-1,v);

}

int arquivo(FILE* arq, coordenada* v){
    int n;
    fscanf(arq,"%i ",&n);

    v=realloc(v,n * sizeof(coordenada));

    int i;

    for(i=0;i<n;i++){
        fscanf(arq,"%i",&(v[i].x));
    }

    for(i=0;i<n;i++){
        fscanf(arq,"%i",&(v[i].y));
    }

    fclose(arq);
    return(n);
}
void main(void){

	FILE *arq;
	int result;

	arq = fopen("trembala.dat", "rb");

	coordenada* v = calloc(v,1*sizeof(coordenada));
    int n = arquivo(arq,v);

	float dist=distanciaSH(n,v);

	printf("%.4f",dist);
    printf("%i-%i",mdist1,mdist2);

}
