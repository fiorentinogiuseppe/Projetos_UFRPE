#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#define N 100
struct reg {
      char* conteudo; // conteúdo
      struct reg *esq;
      struct reg *dir;
} ; // nó
typedef struct reg noh;
int t;
noh* pilha[N];
void criapilha (void) {
   t = 0;
}

noh* desempilha (void) {
   return pilha[--t];
}

void empilha (noh* y) {
   pilha[t++] = y;
}
int pilhavazia (void) {
   return t <= 0;
}
//+++++++++++++++++++++++++++++
int v;
int* pilhaNum[N];
void criapilhaNum(void) {
   v = 0;
}

int desempilhaNum (void) {
   return pilhaNum[--v];
}

void empilhaNum (int y) {
   pilhaNum[v++] = y;
}
int pilhavaziaNum (void) {
   return v <= 0;
}
//+++++++++++++++++++++++++++++

noh* inicializarArvore(){
	return NULL;
}
int arvoreVazia(noh *raiz){
	if(!raiz) return (1);
	else return (0);

}
noh* arv_cria(char c, noh* sae, noh* sad)
{
	 noh* p=(noh*)malloc(sizeof(noh));
	 if(p==NULL) exit(1);
	 p->conteudo = c;
	 p->esq = sae;
	 p->dir = sad;
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
//++++++++++++++
void subArvores(char v[]){
	int i;	
	noh *a;
	noh *e,*d;
	criapilha();
	criapilhaNum();
	for(i=0;i<strlen(v);i++){
		if(v[i]=='+'){
			d=desempilha();
			e=desempilha();	
			a=arv_cria(v[i],e,d);
			empilha(a);
			
		}
		if(v[i]=='*'){
			d=desempilha();
			e=desempilha();	
			a=arv_cria(v[i],e,d);
			empilha(a);
			
		}
		if(v[i]=='-'){
			d=desempilha();
			e=desempilha();	
			a=arv_cria(v[i],e,d);
			empilha(a);
			
		}
		if(v[i]=='/'){
			d=desempilha();
			e=desempilha();	
			a=arv_cria(v[i],e,d);
			empilha(a);
			
		}
		else if(v[i]>='0' && v[i]<='9'){
			empilha(arv_cria(0,inicializarArvore(),inicializarArvore()));
		}
		 while (v[i] >= '0' && (v[i] <= '9')){
			
			valor=10 * desempilha()->conteudo + (v[i++] - '0')+'0';
            		empilha(arv_cria(valor,inicializarArvore(),inicializarArvore()));
       		}


	}
}
void edr(noh* r){
	
	if(r!=NULL){
		edr(r->esq);
		edr(r->dir);
		printf("%c\n",r->conteudo);
	}
}

void main(){
	
	int i;
	char v[]={"5 3 11 +2 8 * * 222 + *"};
	subArvores(v);
	edr(desempilha());
}
