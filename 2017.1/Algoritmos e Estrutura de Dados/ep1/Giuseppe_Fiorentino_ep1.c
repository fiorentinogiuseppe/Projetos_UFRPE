//*******************************************************************************************************************
//Universidade Federal Rural de Pernambuco - Campus Recife
//Disciplina......: Algoritmo e Estrutura de Dados
//Bacharel em Ciência da Computação
//Aluno...........: Giuseppe Fiorentino Neto
//Data............: 13/06/2017
//Numero do EP....: 1
//*******************************************************************************************************************
//*******************************************************************************************************************
//CONSIDERAÇÕES INICIAIS
//Devido ao fato de que o vetor possui uma contagem de indexação iniciada do zero e vai até n-1, onde n é o numero
//total de casas do vetor,algumas variaveis que indicam a inicialização do vetor foram inicializadas com -1(tais como
//primeira, e quarta questao). Porém outras foram iniciadas com 0 e ha outras que iniciam do 1.
//INDICACOES DO QUE SAO AS VARIAVEIS:
//- e,i= inicio do vetor;
//- d,f= fim do vetor;
//- n= total de elementos;
//- m= meio do vetor;
//- x= inteiro qualquer;
//- v= vetor unidimensional;
//- contadorItera= contador para realizar contagens de repetição dos loops iterativos;
//- contadorRecu= contador para realizar contagens de repetição da funções recursivas;
//CREDITOS:
//OS algoritmos como insertionIterativo,selecaoIterativa,heapsortIterativo,quicksortIterativo,todas as funcoes do heap(menos o peneiraR),todas as funções do quick, todas as funções do merge foram algoritmos retirados do site do Prof. Dr. Paulo Feofiloff(https://www.ime.usp.br/~pf/algoritmos/)
//**********************************************************************************************************************

#include<stdio.h>
#include<stdlib.h>
#include<time.h>

void troca (A, B) { int t = A; A = B; B = t; }

long int contadorRecu=0, contadorItera=0;
//Insertion Sort
void insertionIterativo (int n, int v[]){//iterativo
   int i, j, x;
   for (j = 1; j < n; ++j) {//
      x = v[j];
      for (i = j-1; i >= 0 && v[i] > x; --i){
		contadorItera++;         
		v[i+1] = v[i];
	}
      v[i+1] = x;
   }
}
int insertionRecursivo(int ini, int n, int v[]){//Recursivo
	contadorRecu++;
	if(ini>=n-1) return n;
	int m=(ini+n)/2;
	int x=insertionRecursivo(ini, m, v);
	int y=insertionRecursivo(m+1, n, v);
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

//Selection Sort
int Buscamenor (int ini, int n, int v[]) {//função que busca o menor da faixa de vetor enviado para ela
   	if(ini==n) return n;
	int m=(ini+n)/2;
	int x=Buscamenor(ini, m, v);
	int y=Buscamenor(m+1, n, v);

	if(v[x]>v[y]){

		return y;
	}
	else{

		return x;
	}
}

void SelecaoRecursiva(int ini,int n, int v[]){//selection recursivo
	for(int i=ini;i<n;i++) {
		contadorRecu++;
		int menor1=i;
		int menor=Buscamenor(i+1,n,v); 

		if(v[menor1]>v[menor]) {
			int temp=v[menor];
			v[menor]=v[menor1];	
			v[menor1]=temp;
		}
	}
}

void selecaoIterativa (int n, int v[])// selection iterativo
{
   int i, j, min, x;
   for (i = 0; i < n-1; ++i) {
      min = i;
      for (j = i+1; j < n; ++j){
	 contadorItera++;
         if (v[j] < v[min])  min = j;
	}
      x = v[i]; v[i] = v[min]; v[min] = x;
   }
}

//Merge Sort

void mergesortIterativo(int n, int v[])//mergesort recursivo
{
   int p, r;
   int b = 1;
   while (b < n) {
      p = 0;
      while (p + b < n) {	
	contadorItera++;
         r = p + 2*b;
         if (r > n) r = n;
         merge (p, p+b, r, v);
         p = p + 2*b; 
      }
      b = 2*b;
   }
}
void merge (int p, int q, int r, int v[])// merge
{
   int i, j, k, *w;
   w = malloc ((r-p) * sizeof (int));

   for (i = p; i < q; ++i)  w[i-p] = v[i];
   for (j = q; j < r; ++j)  w[r-p+q-j-1] = v[j];
   i = 0; j = r-p-1;
   for (k = p; k < r; ++k)
      if (w[i] <= w[j]) v[k] = w[i++];
      else v[k] = w[j--];
   free (w);
}


void mergesortRecursivo(int ini, int n, int v[]){//funcao mergesort recursivo
	contadorRecu++;
	if(ini>=n-1) return;
	int m=(ini+n)/2;
	mergesortRecursivo(ini,m,v);
	mergesortRecursivo(m+1,n,v);
	merge(ini,m,n,v);
}




//Heap Sort

static void constroiHeap (int m, int v[])//essa função recebe o tamanho do vetor e o vetor e constroi um heapsort
{
   int k; 
   for (k = 1; k < m; ++k) {                   
      // v[1..k] é um heap
      int f = k+1;
      while (f > 1 && v[f/2] < v[f]) {  
         troca (v[f/2], v[f]);          
         f /= 2;                        
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
//Quick Sort

static int separa (int v[], int p, int r)//Recebe um vetor da metade te o fim. Rearranja os elementos do vetor e devolve o maior
{
   int c = v[p], i = p+1, j = r, t;
   while (/*A*/ i <= j) {
      contadorRecu++;
      if (v[i] <= c) ++i;
      else if (c < v[j]) --j; 
      else {
         t = v[i], v[i] = v[j], v[j] = t;
         ++i; --j;
      }
   }
   // agora i == j+1                 
   v[p] = v[j], v[j] = c;
   return j; 
}

void quicksortRecu (int v[], int p, int r)//recursivo
{
   int j;                         
   if (p < r) {                   
      j = separa (v, p, r);       
      quicksortRecu (v, p, j-1);      
      quicksortRecu (v, j+1, r);      
   }
}


void quicksortIterativo (int v[], int p, int r)//iterativo
{
   int j, *pilhap, *pilhar, t;

   pilhap = malloc ((r-p+1) * sizeof (int));
   pilhar = malloc ((r-p+1) * sizeof (int));
   pilhap[0] = p; pilhar[0] = r; t = 0; 
   
   while (t >= 0) {
      contadorItera++;          
      p = pilhap[t]; r = pilhar[t]; --t;
      if (p < r) {
         j = separa (v, p, r);    
         ++t; pilhap[t] = p; pilhar[t] = j-1; 
         ++t; pilhap[t] = j+1; pilhar[t] = r; 
      }
   }
}



//Arquivos
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

int main(void){// possui os dados bases e as chamadas das funcoes
    int n,e,base,f,x,i;
    
    int v[]={70, 60, 50,200,90,500,200,80,0};
    //Insertion
    n=9;
    e=-1;
    contadorRecu=0;
    contadorItera=0;
    //iterativo
    insertionIterativo (n, v);
	    
    //recursivo
    insertionRecursivo(e,n,v);


    //Arquivos
    char *questao;
    questao="Insertion.txt";
    arquivo(n,questao);

    printf("===>Insertion<===\nChamadas Recursivas:%i\nChamadas Iterativas: %i\n",contadorRecu,contadorItera);

    //Selection
    int v1[]={70, 60, 50,200,90,500,200,80,0};
    n=9;
    e=0;
    contadorRecu=0;
    contadorItera=0;
   
    //iterativa
    selecaoIterativa(n, v1);

    //recursiva	
    SelecaoRecursiva(e,n-1,v1);


    //Arquivos
    questao="Selection.txt";
    arquivo(n,questao);

    printf("===>Selection<===\nChamadas Recursivas:%i\nChamadas Iterativas: %i\n",contadorRecu,contadorItera);

    //Merge

    
    int v2[]={70, 60, 50,200,90,500,200,80,0};
    n=9;
    e=0;
    contadorRecu=0;
    contadorItera=0;
   
	
    //iterativo
    mergesortIterativo(n,v2);
    //recursivo
    mergesortRecursivo(e, n-1, v2);


    //Arquivos
    questao="Merge.txt";
    arquivo(n,questao);

    printf("===>Merge<===\nChamadas Recursivas:%i\nChamadas Iterativas: %i\n",contadorRecu,contadorItera);

    //Heap
    int v3[]={70, 60, 50,200,90,500,200,80,0};
    n=9;
    contadorRecu=0;
    contadorItera=0;
    
    //recrusivo
    heapsortRecursivo(n,v3);

    // iterativo
    heapsortIterativo (n, v3);

    //Arquivos
    questao="Merge.txt";
    arquivo(n,questao);

    printf("===>Heap<===\nChamadas Recursivas:%i\nChamadas Iterativas: %i\n",contadorRecu,contadorItera);

    //Quick Sort
    int v4[]={70, 60, 50,200,90,500,200,80,0};
    n=9;
    e=0;
    contadorRecu=0;
    contadorItera=0;

    //iterativo
    quicksortIterativo(v4,e,n-1);
 
    //recursivo
    quicksortRecu(v4,e,n-1);


    //Arquivos
    questao="Quick.txt";
    arquivo(n,questao);
    printf("===>Quick<===\nChamadas Recursivas:%i\nChamadas Iterativas: %i\n",contadorRecu,contadorItera);



    return 0;
}
