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
celula *head;

celula* headCell(char x){
	celula *dummyCell;
	dummyCell=malloc(sizeof(celula));	
	dummyCell->x=x;
	dummyCell->prox=NULL;
	head=dummyCell;
	return dummyCell;
}
void insert(char x,celula *p){
				
	celula *nova;
	nova=malloc(sizeof(celula));	
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
celula* search(char x, celula*b){
	celula *p;
	p=b;
	while(p!=NULL && p->x!=x)
		p=p->prox;
	return p;
}
void printar(celula *p){
	while(p!=NULL){
		printf("%c\n",p->x);
		p=p->prox;
	}

}

void criapilha(char x){
	pi=headCell(x);
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
	else return 0;
}

int stack(char s[]){	
	criapilha('0');
	
	for(i=0; i<strlen(s);++i){

		char c1;
		char c2;
		switch(s[i]){
			case '+':
			{
				if(pilhavazia(pi)==1) return 0;
				c1=desempilha(pi);

				if(pilhavazia(pi)==1) return 0;
				c2=desempilha(pi);
				int soma=((c2-'0')+(c1-'0'));
				char somachar=soma+'0';
				empilha(somachar,pi);
				break;
			}
			case '-':
			{
				if(pilhavazia(pi)==1) return 0;
				c1=desempilha(pi);

				if(pilhavazia(pi)==1) return 0;
				c2=desempilha(pi);

				char subt=((c2-'0')-(c1-'0'))+'0';
				empilha(subt,pi);
				break;
			}
			case '*':
			{
				if(pilhavazia(pi)==1) return 0;
				c1=desempilha(pi);

				if(pilhavazia(pi)==1) return 0;
				c2=desempilha(pi);
				int v1=(c1-'0');
				int v2=(c2-'0');
				char mult=(v2*v1)+'0';
				empilha(mult,pi);
				break;
			}
			case '/':
			{
				if(pilhavazia(pi)==1) return 0;
				c1=desempilha(pi);

				if(pilhavazia(pi)==1) return 0;
				c2=desempilha(pi);

				char div=((c2-'0')/(c1-'0'))+'0';
				empilha(div,pi);
				break;
			}
			default: {
					if(s[i]==' ') break;
					else{
						empilha(s[i],pi);
						break;
					}
			}
		}
		
	}
	return (pi->prox)->x;
}

int main(void){

	char s[]="2 1 2 * +";
	int x=stack(s);
	printf("%c",x);
	return 0;
}
