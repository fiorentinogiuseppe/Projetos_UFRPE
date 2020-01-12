//*******************************************************************************************************************
//Universidade Federal Rural de Pernambuco - Campus Recife
//Disciplina......: Algoritmo e Estrutura de Dados
//Bacharel em Ciência da Computação
//Aluno...........: Giuseppe Fiorentino Neto
//Data............: 23/08/2017
//Numero do EP....: 3
//*******************************************************************************************************************
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int b,N;
int *E;
//ESTRUTURAS
//*******************************************************************************************************************
//Estrutura de celula para lista encadeada com conteudo tipo char
struct reg {
    char conteudo;
    struct reg *prox;
};
typedef struct reg celula;

//Estrutura de celula para lista encadeada com conteudo tipo int
struct regInt {
    int conteudo;
    struct reg *prox;
};
typedef struct regInt celulaInt;

//Estrutura de celula para lista encadeada com conteudo de uma tripla (m, L, p) onde m é o valor do
//parâmetro da expressão na chamada recursiva, L é (um ponteiro para) a lista
//representando a expressão posfixa, e p é o nó na lista onde foi feita a atual
struct regTripla {
    int m;
    celula *L;
    celula *p;

};
typedef struct regTripla Tripla;

//Estrutura de celula para pilha de chamadas recursivas
struct regChamada {
    Tripla conteudo;
    struct reg *prox;
};
typedef struct regChamada celulaChamada;
//*******************************************************************************************************************

//*******************************************************************************************************************
//VARIAVEIS GLOBAIS
celulaInt *stack;
celulaChamada *stackExec;
int tamPilhaExec = 200;
int tam=0;
celula *cabeca;
celula *p;
//*******************************************************************************************************************

//*******************************************************************************************************************
//LISTA ENCADEADA
//Esta função cria a celula cabeça da lista encadeada
celula* celulaCabeca(){
	celula *cabeca;
	cabeca=malloc(sizeof(celula));
	cabeca->prox=NULL;
	return cabeca;
}

// Esta função remove o ultimo elemento da lista
void delete(celula *p){
	celula *apagar;
	apagar=p->prox;
	p->prox=apagar->prox;
	free(apagar);
}


//Esta função imprimi na tela a lista encadeada com o tipo char dada no parametro
void imprime (celula *le) {
   if (le != NULL) {
      printf ("%c", le->conteudo );
      imprime (le->prox);
   }
}

// Esta função insere uma nova celula
// em uma lista encadeada.
void insere(char x, celula *p){
   celula *nova = malloc( sizeof(celula));
   nova->conteudo = x;
   nova->prox = p->prox;
   p->prox = nova;
}
//*******************************************************************************************************************

//*******************************************************************************************************************
//PILHA DE EXECUÇÃO
// Cria uma pilha vazia
void PilhaExecinit() {
	stackExec = malloc(sizeof(celulaChamada));
	stackExec->prox = NULL;

}

//Imprimi na tela a lista uma encadeada dada
void imprimePilhaExec ( celulaChamada *le) {
   if ( le != NULL) {
      Tripla t = le->conteudo;
      printf ("( %i, %i, %i)  ", t.m, t.L, t.p  );
      imprimePilhaExec(le->prox);
   }
}
//Devolve 1 se a pilha estiver vazia e 0 em caso contrário.
int PilhaExecIsEmpty() {
    return (stackExec->prox == NULL);
}

// Empilha na pilha de execução sempre na ultima posicao
void PilhaExecPush(Tripla item) {
    celulaChamada *nova;
    nova = malloc(sizeof (celulaChamada));
    nova->conteudo = item;
    nova->prox  = stackExec->prox;
    stackExec->prox = nova;
    tam++;
    if(tam>tamPilhaExec){
        printf("\nESTOURO DE PILHA");
        exit(0);
    }
}

// Desempilha da pilha de execução sempre a ultima posicao
Tripla PilhaExecPop() {
   celulaChamada *p;
   p = stackExec->prox;
   Tripla x = p->conteudo;
   stackExec->prox = p->prox;
   free (p);
   tam--;
   return x;
}
//*******************************************************************************************************************

//*******************************************************************************************************************
//PILHA DE NUMEROS DA EQUACAO
// Cria uma pilha vazia.
void STACKinit() {
	stack = malloc(sizeof(celulaInt));
	stack->prox = NULL;
}

// Testa se a pilha esta vazia. Devolve 1 se a pilha estiver vazia e 0 em caso contrário.
int STACKempty() {
    return stack->prox == NULL;
}

// Empilha operandos numericos
void STACKpush(int item) {
    celulaInt *nova;
    nova = malloc (sizeof (celulaInt));
    nova->conteudo = item;
    nova->prox  = stack->prox;
    stack->prox = nova;
}

// Retira o item do topo da pilha e devolve o valor do elemento retirado.
int STACKpop() {
    celulaInt *p;
    p = stack->prox;
    int x = p->conteudo;
    stack->prox = p->prox;
    free (p);
    return x;

}

//Percorre a expressão da esquerda para a direita;
//sempre que encontra um operando numérico ou a variável n , empilha; sempre
//que encontra um operador, desempilha dois operandos,  calcula o resultado da
//operação com esses valores, e o empilha.
int  valorPosFix(celula *p)
{
    celula *cel;

    STACKinit();
    for ( cel = p; cel != NULL; cel = cel->prox) {
        if (cel->conteudo == '+'){
            STACKpush(STACKpop() + STACKpop());
        }
        if (cel->conteudo == '*'){
            STACKpush(STACKpop() * STACKpop());
        }
        if (cel->conteudo == '-'){
                if(cel->prox->conteudo >= '0' && (cel->prox->conteudo <= '9')){

                    cel = cel->prox;
                    int i = cel->conteudo - '0';
                    int e = N - i;
                    if(e < 0){
                        e = 0;
                    }
                    if(e >= 0 && e <= b){
                        STACKpush( E[e]);
                    }
                    else{
                        STACKpush(ExpressaoR( e, i, p, cel));
                    }
                    cel = cel->prox;
                }
                else{
                        STACKpush(STACKpop() - STACKpop());
                }

        }
        if (cel->conteudo == '/'){
            STACKpush(STACKpop() / STACKpop());
        }
       if (cel->conteudo >= '0' && cel->conteudo <= '9' ){
            STACKpush(0);
       }
       while (cel->conteudo >= '0' && (cel->conteudo <= '9')){
            STACKpush(10 * STACKpop() + (cel->conteudo - '0'));
            cel = cel->prox;
       }
    }

    return (STACKpop());
}

//Chamadas recursivas E(n − c) , onde c > 0 é um inteiro, e o valor desse
//operando é o valor da expressão E para o parâmetro n− c. somente após obter
//esse valor ele pode retomar o proessamento.
int ExpressaoR(int n, int c, celula* L, celula* p){
    Tripla t;
    t.m = n;
    t.L = L;
    t.p = p;

    PilhaExecinit();
    PilhaExecPush(t);
    printf("\n");
    imprimePilhaExec(stackExec->prox);

    while(!PilhaExecIsEmpty()){

            if(n < 0 || n > b){

                n = n-c;
                if(n < 0){
                    n=0;
                }
                t.m = n;
                t.L = L;
                t.p = p;
                PilhaExecPush(t);
                printf("\n");
                imprimePilhaExec(stackExec->prox);

            }else{

                PilhaExecPop();
            }
    }

    printf("\nValor calculado: %i", E[n]);
    return(E[n]);
}
//*******************************************************************************************************************

//*******************************************************************************************************************
//OUTRAS FUNCOES
void arquivos(){//carrega os arquivos do arquivo lep.in
    FILE *arq;
    int i;
    int e[b+1];
    char x;
    int tamanho = 0;

    arq = fopen("lep.in", "rb");
    fscanf(arq,"%i ",&b);
    for(i = 0; i <= b; i++){
        fscanf( arq, "%i\n", &e[i]);
    }

    for(i = 0; i <= b; i++){
        printf("%i\n", e[i]);
    }

    E = e;
    fscanf( arq,"%c", &x);
    cabeca = celulaCabeca(x);
    insere( x, cabeca);
    p = cabeca->prox;

    while(x != 'a' ){
        fscanf(arq,"%c",&x);
        if(x != 'a'){
            insere( x, p);
            p = p->prox;

            tamanho++;
        }
    }

    fscanf(arq,"%i",&N);
    fclose(arq);
}
//*******************************************************************************************************************

//*******************************************************************************************************************
//MAIN
void main(void)//chama a funcao que imprime a cabeça da expressao e calcula a expressao desejada
{
    arquivos();
    printf("%i\n", N);
    imprime(cabeca->prox);
    printf("\n %d",valorPosFix(cabeca));
}
//*******************************************************************************************************************
