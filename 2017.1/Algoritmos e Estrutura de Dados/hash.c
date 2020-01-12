#include<stdio.h>
#include<stdlib.h>
#define M 13
// Tamanho da tabela.

#define hash(v, M) (v % M)
// Transforma uma chave v em um �ndice no intervalo 0..M-1.

typedef struct {
   int chave;
   int valor;
} tipoObjeto;

tipoObjeto objetonulo;
//objetonulo.chave=0;

//contagem 
struct reg {
   int     chave, ocorr; 
   struct reg *prox;
};
typedef struct reg celula;

celula **tb;
tb = (celula *)malloc (M * sizeof (celula *));

//contagem por sondagem linear
struct reg {
   int     chave, ocorr; 
   struct reg *prox;
};
typedef struct reg celula;

struct STnode {
   tipoObjeto obj; 
   struct STnode * next;
} ;
typedef struct STnode *link;

link *tab;
void STinit() 
{ 
   int h;
   tab = malloc(M * sizeof (link));
   for (h = 0; h < M; h++) 
      tab[h] = NULL; 
}
void STinsert(tipoObjeto obj) 
{ 
   int h, v;
   v = obj.chave;
   h = hash(v, M);
   link novo = malloc(sizeof (link));
   novo->obj = obj;
   novo->next = tab[h];
   tab[h] = novo;
}

tipoObjeto STsearch(int v) 
{ 
   link t;
   int h;
   h = hash(v, M);
   for (t = tab[h]; t != NULL; t = t->next) 
      if (t->obj.chave == v) break;
   if (t != NULL) return t->obj;
   return objetonulo;
}
void contabiliza (int ch) {
   int h = hash (ch, M);
   celula *p = tb[h]; 
   while (p != NULL && p->chave != ch)
      p = p->prox;
   if (p != NULL) 
      p->ocorr += 1;
   else {
      p = malloc (sizeof (celula));
      p->chave = ch;
      p->ocorr = 1;
      p->prox = tb[h];       
      tb[h] = p;       
   }
}
void contabilizaSond (int ch) {
   int h = hash (ch, M);
   cell *p = tb[h]; 
   while (p != NULL && p->chave != ch)
      p = p->prox;
   if (p != NULL) 
      p->ocorr += 1;
   else {
      p = malloc (sizeof (cell));
      p->chave = ch;
      p->ocorr = 1;
      p->prox = tb[h];       
      tb[h] = p;       
   }
}
void display() {
   int i = 0;
	
   for(i = 0; i<M; i++) {
	
      if(tab[i] != NULL)
         printf(" (%d,%d)",tab[i]->obj.chave,tab[i]->obj.valor);
      else
         printf(" ~~ ");
   }
	
   printf("\n");
}
void main(void){
	int v[]={17,21,19,4,26,30,37};
	tipoObjeto novo;
	int i;	
	STinit();
	for(i=0;i<7;i++) {
		novo.chave=10*i;
		novo.valor=v[i];
		STinsert(novo);
	}
	display();

}
