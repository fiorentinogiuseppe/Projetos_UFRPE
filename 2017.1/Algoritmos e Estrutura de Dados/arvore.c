#include<stdio.h>
#include<stdlib.h>
#define troca (A, B) { int t = A; A = B; B = t; }
#define N 100

//arvore
typedef struct reg {
   int conteudo;
   struct reg *pai;
   struct reg *esq, *dir;
} noh;
noh fila[N];
noh* inicializarArvore(){
	return NULL;
}
int arvoreVazia(noh *raiz){
	if(!raiz) return (1);
	else return (0);

}
noh* arv_cria(int c, noh* sae, noh* sad)
{
	 noh* p=(noh*)malloc(sizeof(noh));
	 if(p==NULL) exit(1);
	 p->conteudo = c;
	 p->esq = sae;
	 p->dir = sad;
	 sae->pai=p;
	 sad->pai=p;
	 return p;
}
noh* arv_libera (noh* a){
 if (!arv_vazia(a)){
	 arv_libera(a->esq); /* libera sae */
	 arv_libera(a->dir); /* libera sad */
	 free(a); /* libera raiz */
 }
 return NULL;
}
int arv_vazia (noh* a)
{
 	return a==NULL;
}
//fila de prioridade
void corrige-subindo(int A[], int m){
	int i=m;
	/*while(i>=2 && A[i/2]<A[i]){
		troca(A[i/2],A[i]);
		i=i/2;	
	}*/
	 if (r != NULL) {
      		erd (r->esq);
      		erd (r->dir); 
		if(r->dir->conteudo < r-esq->conteudo){
			if(r->conteudo<r->esq->conteudo){
				int tmp=r->conteudo;
				r->conteduo=r->esq->conteduo;
				r->esq->conteduo=tmp;
			}
		}
		else if(r->dir->conteudo > r-esq->conteudo){
			if(r->conteudo < r->dir->conteudo){
				int tmp=r->conteudo;
				r->conteduo=r->dir->conteduo;
				r->dir->conteduo=tmp;
			}
		}
   	}

}
void corrige-descendo(int A[], int n, int i){
	int j=i;
	/*while(2*j<=n){
		int f=2*j;
		if(j<n && A[f]<A[f+1])
			f=f+1;
		if(A[j]>=A[f])
			j=n;
		else 
			troca(A[j], A[f]);
		j=f;
	}*/
	 if (r != NULL) {
		if(r->dir->conteudo < r-esq->conteudo){
			if(r->conteudo<r->esq->conteudo){
				int tmp=r->conteudo;
				r->conteduo=r->esq->conteduo;
				r->esq->conteduo=tmp;
			}
		}
		else if(r->dir->conteudo > r-esq->conteudo){
			if(r->conteudo < r->dir->conteudo){
				int tmp=r->conteudo;
				r->conteduo=r->dir->conteduo;
				r->dir->conteduo=tmp;
			}
		}
		erd (r->esq);
      		erd (r->dir); 
   	}

}
void criafila (void) {
   p = 0; u = 0;
}

int filavazia (void) {
   return p >= u;
}

noh* tiradafila (void) {
   noh i= fila[p++];
   corrige-descendo(A,n,i);
   return i;
}

void colocanafila (noh y) {
   fila[u++] = y;
   corrige-subindo(A,i);
}
void erd_i (noh r) {
   criafila ();  // fila de nós 
   colocanafila (r);//modificar
   while (1) {
      x = tiradafila ();//remover da fila
      if (x != NULL) {
         colocanafila(x);
         colocanafila (x->esq);
      }
      else {
         if (filavazia ()) break;
         x = tiradafila ();//remover da fila
         printf ("%d\n", x->conteudo);
         colocanafila (x->dir);
      }
   }

}

