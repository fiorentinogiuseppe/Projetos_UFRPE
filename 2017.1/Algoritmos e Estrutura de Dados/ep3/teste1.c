#include<stdio.h>
#include<time.h>
#include<stdlib.h>
#include<string.h>

struct no{
	char x;
	struct no *prox;
};
typedef struct no celula;

struct recu{
	int m;
	int *L;
	int *p;
	
};
typedef struct no rcrs;


int t;
int i;
int N;
celula *pi;
celula *pib;
celula *E;


void insert(char x,celula *p){
				
	celula *nova;
	nova=(celula*) malloc(sizeof(celula));	
	nova->x=x;
	nova->prox=p->prox;
	p->prox=nova;

}
void delete(celula *p){
	celula *apagar;
	apagar=p->prox;
	p->prox=apagar->prox;
	free(apagar);
}

void criapilha(celula *p){
	celula *head;
	head=(celula*) malloc(sizeof(celula));	
	head->prox=NULL;
	p=head;
printf("%p",p);
}
void empilha(char x,celula *p){
	insert(x,p);
}
char desempilha(celula *p){
	celula *deletar;
	deletar=p->prox;
	char x=deletar->x;
	p->prox=deletar->prox;
	free(deletar);
	return x;
}
int pilhavazia(celula *p){
	if(p->prox==NULL) return 1;
}

int recursao(int x,celula *p){
		
	
}
/*
int cases(char x,celula *p){	
	
	char c1;
	char c2;
	celula *a=pi;
	a=a->prox;
	while(a!=NULL){
		switch(a->x){
				case '+':
				{
					if(pilhavazia(p)==1) return 0;
					c1=desempilha(p);

					if(pilhavazia(p)==1) return 0;
					c2=desempilha(p);
					int soma=((c2-'0')+(c1-'0'));
					char somachar=soma+'0';
					empilha(somachar,p);
					break;
				}
				case '-':
				{
					if(pilhavazia(p)==1) return 0;
					c1=desempilha(p);

					if(pilhavazia(p)==1) return 0;
					c2=desempilha(p);

					char subt=((c2-'0')-(c1-'0'))+'0';
					if(a->prox->x==' ' ) empilha(subt,p);
					else {

						int calculo=N-(a->prox->x);
						recursao(calculo,p);
					}
					break;
				}
				case '*':
				{
					if(pilhavazia(p)==1) return 0;
					c1=desempilha(p);

					if(pilhavazia(p)==1) return 0;
					c2=desempilha(p);
					int v1=(c1-'0');
					int v2=(c2-'0');
					char mult=(v2*v1)+'0';
					empilha(mult,p);
					break;
				}
				case '/':
				{
					if(pilhavazia(p)==1) return 0;
					c1=desempilha(p);

					if(pilhavazia(p)==1) return 0;
					c2=desempilha(p);

					char div=((c2-'0')/(c1-'0'))+'0';
					empilha(div,p);
					break;
				}
				default: {
						if(x==' ') break;
						else{
							empilha(x,p);
							break;
						}
				}
			}
			a=a->prox;
	}
	return (p->prox)->x;
}*/
int arquivo(){
    FILE *arq;
    arq=fopen("lep.in","rb");
    int n;
    int val;
    fscanf(arq,"%i ",&n);

    for(i=0;i<n;i++){
	fscanf(arq,"%i ",&val);
	empilha(val+'0',pib);
	getchar();
    }
    printf("oie");
	getchar();
    for(i=0;i<n;i++){
	fscanf(arq,"%i ",&val);
	if(val!='\0' || val!='\r');	empilha(val+'0',E);	
	printf("%i\n",val);
    }
    fscanf(arq,"%i ",&N);
   return n;
}
void iniciarPilhas(){
	criapilha(pi);
	printf("\ninin%i\n",pi);
	getchar();

}
int main(void){
	iniciarPilhas();
	int n=arquivo();



	//int x=chamadas();
	//printf("%c",x);
	return 0;
}
