#include<stdio.h>
#include<stdlib.h>
#define M 8191
// Tamanho da tabela.

#define hash(v, M) (v % M)
// Transforma uma chave v em um �ndice no intervalo 0..M-1.

typedef struct {
   int chave;
   int ocorr;
} tipoObjeto;

tipoObjeto objetonulo;
// Todas as chaves "v�lidas" s�o estritamente positivas.

// Defini��o de um n� das listas de colis�es.
typedef struct STnode *link;
struct STnode {
   tipoObjeto obj; 
   link       next;
} ;

// Tabela que aponta para as M listas de colis�es.
link *tab;
// Inicializa uma tabela de s�mbolos que, espera-se, armazenar� 
// cerca de 50000 objetos. A espinha dorsal da tabela ser� um 
// vetor tab[0..M-1].
//
void salvar(int valor){
	FILE*arq;
	arq=fopen("numeros1.txt","r");
	if(arq==NULL) arq=fopen("numeros1.txt","w");
	else arq=fopen("numeros1.txt","a");
	printf("%i",valor);
	fprintf(arq,"%i\n",valor);
	
}
void STinit() 
{ 
   int h;
   tab = malloc(M * sizeof (link));
   for (h = 0; h < M; h++) 
      tab[h] = NULL; 
}

// Insere obj na tabela de s�mbolos.
//
void STinsert(tipoObjeto obj) 
{ 
	int h, v;
	v = obj.chave;
	h = hash(v, M);
	link t=tab[h];
	for (t = tab[h]; t != NULL; t = t->next) {      
		if (t->obj.chave == v) break;
	}	
	if (t != NULL) 
      		t->obj.ocorr++;
	if(t==NULL){
		obj.ocorr=1;
		link novo = malloc(sizeof (link));
		novo->obj = obj;
		novo->next = tab[h];
		tab[h] = novo;
		salvar(obj.chave);
	}
}

// Devolve um objeto cuja chave � v. Se tal objeto n�o existe,
// a fun��o devolve um objeto fict�cio com chave nula.
//
tipoObjeto STsearch(int v) 
{ 
   link t;
   int h;
   h = hash(v, M);
   for (t = tab[h]; t != NULL; t = t->next) {      
	if (t->obj.chave == v) break;
	}

   if (t != NULL) return t->obj;
	
   return objetonulo;
}

static void imprime ()
{	
	link p;
	int i=0;
	for (i=0; i<M; i++)
		for (p=tab[i]; p!=NULL; p=p->next)
			printf("key%i-ocorr%i\n",p->obj.chave,p->obj.ocorr);
}

int main (void)
{	objetonulo.chave = 0;
	STinit();
	FILE* fp;
	fp=fopen("numeros.txt","rt");
	if(fp==NULL) {
		printf("ERRO na abertura do arquivo.\n");
		return 0;
	}
	int c;
	fscanf(fp,"%d",&c);
	tipoObjeto novo;
	while(!feof(fp)){
		novo.chave=c;
		STinsert(novo);
		fscanf(fp,"%d",&c);
	}
	imprime();
	return 0;
}
