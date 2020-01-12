#include<stdio.h>
#include<time.h>
#include<stdlib.h>


struct no{
	int x;
	struct no *prox;
};
typedef struct no celula;
celula* headCell(int x){
	celula *dummyCell;
	dummyCell=malloc(sizeof(celula));	
	dummyCell->x=x;
	dummyCell->prox=NULL;
	return dummyCell;
}
void insert(int x,celula *p){

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
celula* search(int x, celula*b){
	celula *p;
	p=b;
	while(p!=NULL && p->x!=x)
		p=p->prox;
	return p;
}
void printar(celula *p){
	while(p!=NULL){
		printf("%i\n",p->x);
		p=p->prox;
	}
}
void ll(int n){
	srand(time(NULL));  
	celula *head=headCell(rand()%10);
	celula *p;
	int i;
	p=head;
	for(i=0;i<n;i++){
		insert(rand()%10,p);
		p=p->prox;	
	}
	printar(p);
}
/*
int t;
int i;
char pilha[5];
void criapilha(){
	t=0;
}
void empilha(char x){
	pilha[t++]=x;
}
char desempilha(){

	return pilha[--t];
}
int pilhavazia(){
	return (t)<=0;
}

int stack(char s[]){

	
	criapilha(t);

	for(i=0; s[i]!='\0';++i){
		char c;
		switch(s[i]){
			case ')':
			{
				if(pilhavazia()) return 0;
				c=desempilha();
				if(c!='(') return 0;
				break;
			}
			case ']':
			{
				if(pilhavazia()) return 0;
				c=desempilha();
				if(c!='[') return 0;
				break;
			}
			default: {

				empilha(s[i]);
			}
		}
		
	}	
	return pilhavazia();
}
*/
int main(void){

	char s[]={'(','(',')',')'};
	int i;

	ll(5);

	return 0;
}
