#include<stdio.h>
#include<stdlib.h>
#define less(A, B) (A < B)

struct reg {
      int conteudo; 
      struct reg *prox;

};
typedef struct reg celula;  // célula

celula *cabeca1;
celula *cabeca2;


celula* head(){
   celula *le;//cabeça da celula
   le = malloc (sizeof (celula));
   le->prox = NULL;
   return le;
}
void remover (celula *p)
{
   celula *lixo;
   lixo = p->prox;
   p->prox = lixo->prox;
   free (lixo);
   
}
void inserir (int x, celula *p)
{
   celula *nova;
   nova = malloc (sizeof (celula));
   nova->conteudo = x;
   nova->prox = p->prox;

   p->prox = nova;

}

celula* intercala (celula* a,celula* b)
{
   struct reg head;
    celula * c = &head;
c= malloc(sizeof(celula)); 
    while (a != NULL && b != NULL){
	
     if (less(a->conteudo,b->conteudo)) {
	printf("if:a:%i:-b:%i:\n",a->conteudo,b->conteudo);
	getchar();
         c->prox = a;
         c = a; a = a->prox;
     }
     else {
	printf("else:a:%i:-b:%i:\n",a->conteudo,b->conteudo);
	getchar();
         c->prox = b;
         c = b; b = b->prox;
     }
    }
    c->prox = (a == NULL) ? b : a;
    return c->prox;
}
void print (celula *le)
{
   celula *p;
   p = le;
   while (p != NULL ) {
	printf("%i",p->conteudo);
	getchar();
      p = p->prox; 
	}
}
void main(void){
//+++++++++++++++++++++++++++++++
	cabeca1=head();
	cabeca2=head();

//+++++++++++++++++++++++++++++++
	celula * lista1;
	inserir(7,cabeca1);
	lista1=cabeca1->prox;
	inserir(5,lista1);
	inserir(3,lista1);
	inserir(1,lista1);

//+++++++++++++++++++++++++++++++
	celula * lista2;
	inserir(8,cabeca2);
	lista2=cabeca2->prox;
	inserir(6,lista2);
	inserir(4,lista2);
	inserir(2,lista2);
//+++++++++++++++++++++++++++++++

	celula *c=intercala(cabeca1,cabeca2);
	print(c);

}
