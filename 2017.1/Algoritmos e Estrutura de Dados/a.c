#include<stdio.h>
#include<stdlib.h>

////////////////////////////////////////////////////////////////////////////TODO ADD ponteiro voltando////////////////////////
struct noInt{//estrutura para da pilha encadeada
	int conteudo;
	struct no *prox;
	struct no *ant;
};
typedef struct no celulaInt;
struct no{//estrutura para da pilha encadeada
	char conteudo;
	struct no *prox;
	struct no *ant;
};
typedef struct no celula;
struct stack{//estrutura para da pilha de chamadas
	char m;
	celula *L;
	celula *p;
};
typedef struct no celulaRecu;
celulaInt *cpInt=NULL;
celulaInt *pInt=NULL;
celula *cpi=NULL;
celula *pi=NULL;//ponteiro para a cabeça da lista que esta relacionada com a pilha
celula *bs=NULL;//ponteiro para a cabeça da lista que esta relacionada com a base
celula *CE=NULL;
celula *E=NULL;//ponteiro para a cabeça da lista que esta relacionada com a expressao
//celula *cnum=NULL;
//celula *num=NULL;
char N,n;
//###################################################
//Nesta secao estao as funcoes relacionadas com a lista encadeada

celula* head(){//cria a cabeça da lista
	celula *le;
	le = malloc (sizeof (celula));
	le->prox = NULL;
	le->ant=NULL;
	return le;
}


void inserir (char x, celula *p)//insere na lista um novo vaor
{
   celula *nova;
   nova = (celula*)malloc (sizeof (celula));
   nova->conteudo = x;
   nova->prox = p->prox;
   p->prox = nova;
   nova->ant=p;

}

char remover (celula *p)//remove um valor da lista e retorna o valor que  foi removido
{
   char retorno = p->prox->conteudo;

   celula *lixo;
   lixo = p->prox;
   p->prox = lixo->prox;

   return retorno;
}
//###################################################

//Nesta secao estao as funcoes relacionadas com a lista encadeada com int

celulaInt* headInt(){//cria a cabeça da lista
	celulaInt *le;
	le = malloc (sizeof (celulaInt));
	le->prox = NULL;
	le->ant=NULL;
	return le;
}


void inserirInt (int x, celulaInt *p)//insere na lista um novo vaor
{
   celulaInt *nova;
   nova = (celulaInt*)malloc (sizeof (celulaInt));
   nova->conteudo = x;
   nova->prox = p->prox;
   p->prox = nova;
   nova->ant=p;

}

int removerInt (celulaInt *p)//remove um valor da lista e retorna o valor que  foi removido
{
   int retorno = p->prox->conteudo;

   celulaInt *lixo;
   lixo = p->prox;
   p->prox = lixo->prox;

   return retorno;
}
//###################################################


//###################################################
//Nesta secao estao as funcoes relacionadas com a pilha usando lista encadeada
celula* criapilha () {//cria a pilha criando a cabeça da lista
   return head();
}

void empilha (char x,celula *p) {//funcao que em pilha
   inserir (x,p);
}

char desempilha (celula *p) {//funcao que desempilha e recebe o valor retornado na hora de remover da lista e retorna esse valor
   return remover (p);
}

int pilhavazia (celula *p) {//testa se a pilha esta vazia
   if(p->prox==NULL) return 1;//sim a pilha ta vazia
   return 0;//nao a pilha nao esta vazia
}
//###################################################

//###################################################
//Nesta secao estao as funcoes relacionadas com a pilha de int usando lista encadeada
celulaInt* criapilhaInt () {//cria a pilha criando a cabeça da lista
   return headInt();
}

void empilhaInt (int x,celulaInt *p) {//funcao que em pilha
   inserirInt (x,p);
}

int desempilhaInt (celula *p) {//funcao que desempilha e recebe o valor retornado na hora de remover da lista e retorna esse valor
   int k=removerInt (p);
   return k;
}

int pilhavaziaInt (celulaInt *p) {//testa se a pilha esta vazia
   if(p->prox==NULL) return 1;//sim a pilha ta vazia
   return 0;//nao a pilha nao esta vazia
}
//###################################################

//###################################################
//Outras funcoes
void printInt (celula *le)//printar
{
   celula *p;
   p = le;
   p = p->prox;
   while (p != NULL ) {
              printf("%i\n",p->conteudo);
	      p = p->prox;
	}
}
void print (celula *le)//printar
{
   celula *p;
   p = le;
   p = p->prox;
   while (p != NULL ) {
              printf("%c\n",p->conteudo);
	      p = p->prox;
	}
}
void arquivos(){//carrega os arquivos do arquivo lep.in
	FILE *arq;
	arq=fopen("lep.in","rb");
	if(arq==NULL){
		printf("ERRO AO ABRIR O ARQUIVO");
		exit(0);
	}
	int i,contador=0;
	int val;
	char val1;
	fscanf(arq,"%i ",&n);
	for(i=0;i<=n;i++){
		fscanf(arq,"%i ",&val);
		empilha(val+'0',bs);
	}

   	 while(!feof(arq)){

		fscanf(arq,"%c",&val1);
		if(val1!='\n' && val1!='\0' && val1!='\r'){
			inserir(val1,E);
			E=E->prox;
		}

	}
    	N=remover(E->ant);
/*
	celula *p;
	p = E;
	p = p->prox;
	while (p != NULL ) {

		if(p->conteudo>='0' && p->conteudo<='9') empilha(p-> conteudo,num);

		p=p->prox;
	}*/
}

void inicializacao(){//inicializa dos valores dos ponteiros
	cpi=criapilha();
	empilha('a',cpi);
	pi=cpi->prox;
	bs=head();
	cpInt=criapilha();
	empilha('a',cpInt);
	pInt=cpInt->prox;
	CE=head();
	inserir('a',CE);
	E=CE->prox;
}
//###################################################
//###################################################
//calculadora
int leitor(celula*k)
{
	   int c1;
	   int c2;
	   celula *p;
	   p = k;
	   p = p->prox;


	   while (p != NULL ) {
		char x=p->conteudo;

		if(x=='+'){

			if(pilhavaziaInt(cpi)!=1) {
				c1=(desempilhaInt(cpi));
				c2=(desempilhaInt(cpi));

				empilhaInt((c2+c1),cpi);
			}

		}
		else if(x=='*'){
			if(pilhavaziaInt(cpi)!=1) {
				c1=(desempilhaInt(pi));
				c2=(desempilhaInt(pi));
				empilhaInt((c2*c1),cpi);
			}
		}
		else if(x=='-'){

			/*if(p->prox->conteudo!=' ') {
				 p = p->prox;
				 int i = p->conteudo - '0';
				 int e = N - i;
				 if(e < 0){
				       e = 0;
				 }
				 if(e >= 0 && e <= b){
				       empilha( E[e]);
				 }
				 else{
				       empilha(recursao( e, i, k, p),pi);
				 }
				 p = p->prox;
				 //recursao((E->prox->conteudo-'0') - (N-'0'));
			}*/
			//else{


				if(pilhavaziaInt(cpi)!=1) {
					c1=(desempilhaInt(cpi));
					c2=(desempilhaInt(cpi));

					empilhaInt((c2-c1),cpi);
				}
			//}
		}
		else if(x=='/'){

			if(pilhavaziaInt(cpi)!=1) {
					c1=(desempilhaInt(cpi));
					c2=(desempilhaInt(cpi));

					empilhaInt((c2/c1),cpi);
				}
		}/*
		else{

			if(x=='n') x=N;
			if(x!=' ') empilhaInt(x,cpi);
		}*/
		if (x >= '0' && x <= '9' ){
            		empilhaInt(0,cpi);
        }

        while (x >= '0' && (x<= '9')){
                printf("%c",p->conteudo);
                getchar();
                empilhaInt(10 * desempilhaInt(cpi) + (x - '0'),cpi);
                p = p->prox;
	       }
		p = p->prox;

	    }

printf("%c",desempilhaInt(cpi));
	return desempilhaInt(cpi);
}

int recursao(int n, int c, celula* L, celula* p){




}
//###################################################
void main(void){
	inicializacao();
	arquivos();
//printf("%p",E->ant);

int k=leitor(CE->prox);

	//printf();
	//print(pi);
}
