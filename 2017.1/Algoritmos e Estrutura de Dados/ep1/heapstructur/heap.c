#include<stdio.h>
long int contadorRecu=0, contadorItera=0;
void troca (A, B) { int t = A; A = B; B = t; }
static void 
constroiHeap (int m, int v[])//essa função recebe o tamanho do vetor e o vetor e constroi um heapsort
{
   int k; 
   for (k = 1; k < m; ++k) {                   
      // v[1..k] é um heap
      int f = k+1;
      while (f > 1 && v[f/2] < v[f]) {  // 5
         troca (v[f/2], v[f]);          // 6
         f /= 2;                        // 7
      }
   }
}
void peneiraR(int v[], int n, int i) // essa função recebe o tamanho do vetor e o vetor e reorganiza este de forma que o pai seja maior que os filhos. Isto é feito de forma recursiva
{
    contadorRecu++;
    int f=i*2;

    if(f>n)
    {
        return;
    }

    if (f < n && v[f] < v[f + 1]) ++f;
    if (v[i] >= v[f]) return;

    int aux = v[i];
    v[i] = v[f];
    v[f] = aux;

    peneiraR(v, n, f);
}


void peneira (int m, int v[])// essa função recebe o tamanho do vetor e o vetor e reorganiza este de forma que o pai seja maior que os filhos. Isto é feito de forma iterativo
{ 
   int p = 1, f = 2, t = v[1];
   while (f <= m) {
	contadorItera++;
      if (f < m && v[f] < v[f+1])  ++f;
      if (t >= v[f]) break;
      v[p] = v[f];
      p = f, f = 2*p;
   }
   v[p] = t;
}




void heapsortIterativo (int n, int v[])//esta função recebe o tamanho do vetor e o vetor e chama as funcoes auxiliares para realizar a ordenação. O diferencial desta função é que ela chama a função peneira que trabalha de forma iterativa
{
   int m;
   constroiHeap (n, v);
   for (m = n; m >= 2; --m) {
      troca (v[1], v[m]);
      peneira (m-1, v);
   }
}

void heapsortRecursivo (int n, int v[])//esta função recebe o tamanho do vetor e o vetor e chama as funcoes auxiliares para realizar a ordenação.O diferencial desta função é que ela chama a função peneira que trabalha de forma recursiva.
{
   int m;
   constroiHeap (n, v);
   for (m = n; m >= 2; --m) {
      troca (v[1], v[m]);
      peneiraR (v, m - 1, 1);
   }
}

void arquivo(int n,char *questao){

    int diferenca=contadorItera-contadorRecu;
    if(diferenca<0) diferenca=diferenca*(-1);

    FILE *arq;
    arq=fopen(questao,"r");
    if(arq==NULL){
        arq=fopen(questao,"w");
	fprintf(arq,"#Iterativo-#Recursivo-Diferenca-#Tamanho;\n%i		%i	%i	%i",contadorItera,contadorRecu,diferenca,n);
    }
    else{
	arq=fopen(questao,"a");
    	fprintf(arq,"\n%i		%i	%i	%i",contadorItera,contadorRecu,diferenca,n);
    }

}
void main(void)
{
	int n=10000000;
	srand(time(NULL));  
	int v[n];
	int v1[n];
	for(int i=0;i<n;i++) {
		v[i]=rand();
		v1[i]=v[i];
	}
	    
	    //recrusivo
	    heapsortRecursivo(n,v);

	    // iterativo
	    heapsortIterativo (n, v1);

	    //Arquivos
	char *questao;	    
	questao="Merge.txt";
	    arquivo(n,questao);

	    printf("===>Heap<===\nChamadas Recursivas:%i\nChamadas Iterativas: %i\n",contadorRecu,contadorItera);
	
}
