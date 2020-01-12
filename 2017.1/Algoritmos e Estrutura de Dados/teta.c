typedef struct lista {
   int info;
   struct lista* prox;
} Lista;

Lista* lista_cria() {
   return NULL;
}

Lista* lista_insere (Lista* l, int i) {
   Lista* novo = (Lista*) malloc(sizeof(Lista));
   novo->info = i;
   novo->prox = l;
   return novo;
}

void lista_imprime (Lista* l) {
   Lista* p; /* variavel auxiliar para percorrer a lista */
   for (p = l; p != NULL; p = p->prox)
       printf("info = %d\n", p->info);
}
Lista* inverte (Lista * lista){
	if(lista->prox==NULL) return lista;
	Lista* l=malloc(sizeof(Lista));
	l=inverte(lista->prox);
	insere(l,lista->info);
}
void main(void) {
   Lista* l;
   int n;
   l = lista_cria();
   l = lista_insere(l, 23);
   l = lista_insere(l, 45);
   l = lista_insere(l, 67);
   l = lista_insere(l, 89);
   l = lista_insere(l, 1011);
   printf("\nLista:\n");
   lista_imprime(l);
   Lista * inv;
   inv=lista_cria();
   l = lista_insere(inv, 0);
   inverte(inv);
   getchar();
   
}
