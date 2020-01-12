#include<stdio.h>
#include<time.h>
#include<stdlib.h>
#include<string.h>

struct no{
	char x;
	struct no *prox;
};
typedef struct no celula;

int t;
int i;
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
celula* headList(){
	celula *head;
	head=(celula*) malloc(sizeof(celula));	
	head->prox=NULL;
	return head;
}
celula* criapilha(){
	return headList();
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

int recursao(int n){	
	celula *b;
	b=pib;
	b=b->prox;
	int contador=0;
	while(b!=NULL){
		if(n==contador){
			return  b->x;		
		}
		b=b->prox;
		contador++;
	}	
	celula *c=E;
	while(c!=NULL){	
		c=c->prox;
		printf("%c",c->x);
	}

	return 3;//modificar
}
/*
	while(c!=NULL){	
	
		char c1;
		char c2;	
		char c3;
		c=c->prox;
printf("%c",c->x);

		if(c->x!=' ') {
			switch(c->x){
				case '+':
				{
					if(pilhavazia(c)==1) return 0;
					c1=desempilha(c);

					if(pilhavazia(c)==1) return 0;
					c2=desempilha(c);
					int soma=((c2-'0')+(c1-'0'));
					char somachar=soma+'0';
					empilha(somachar,c);
					break;
				}

				case '-':
				{
					if(pilhavazia(p)==1) return 0;
					c1=desempilha(p);
					if(c1!=' '){
						if(pilhavazia(p)==1) return 0;
						c2=desempilha(p);

						char subt=((c2-'0')-(c1-'0'))+'0';
						empilha(subt,p);				
					}
					else{
						c3=desempilha(p);
						if(c3>n) recursao(0);
						recursao(n-(p->x));
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
						if(c->x==' ') break;
						else{
							empilha(c->x,c);
							break;
						}
				}
			}



			
		}
}
		*/

int arquivo(){
    FILE *arq;
    arq=fopen("lep.in","rb");
	
    char n;
    char val;
    int contador; 

    fscanf(arq,"%c ",&n);
    printf("%c\n",n);
    contador=n-'0';

    for(i=0;i<contador;i++){
	fscanf(arq,"%c ",&val);
	printf("val%cval\n",val);	
	empilha(val,pib);

    }

    do
    {

	fscanf(arq,"%c",&val);
	if(val!='	' && val!='\n') insert(val,E);
	printf("aa%c",E->prox->x);
	getchar();
     }while(!feof(arq));

   return n-'0';
}
void iniciarPilhas(){
	pi=criapilha();
	pib=criapilha();
	E=headList();
}
int main(void){
	iniciarPilhas();
	int n=arquivo();
	//recursao(n);

	return 0;
}
