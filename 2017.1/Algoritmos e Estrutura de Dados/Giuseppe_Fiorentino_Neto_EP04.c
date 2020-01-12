#include<stdio.h>
#include<stdlib.h>
#include<ctype.h>
#include<string.h>
#define NPAL 64
#define NTAB 127

struct palavra{
	char pal[NPAL];
	int ocorr;
	struct palavra* prox;
};

typedef struct palavra Palavra;

typedef Palavra* Hash[NTAB];

static int le_palavra(FILE* fp, char*s){
	int i=0;
	int c;
	while((c=fgetc(fp)) != EOF){
		if(isalpha(c))
			break;
	}
	if(c==EOF)
		return 0;
	else
		s[i++]=c;
	while(i<NPAL-1 && (c=fgetc(fp)) !=EOF && isalpha(c))
		s[i++]=c;
	s[i]='\0';
	return 1;
}
static void inicializa(Hash tab) {
   int h;
   for (h = 0; h < NTAB; h++)
      tab[h] = NULL;
}
int hash(char *s) {
   int i, total = 0;
   for (i = 0; s[i] != '\0'; i++)
      total += s[i];
   return total%NTAB;
}

static Palavra *acessa(Hash tab, char*s){
	Palavra* p;
	int h=hash(s);
	for(p=tab[h];p!=NULL; p=p->prox){
		if(strcmp(p->pal,s)==0)
			return p;
	}
	p=(Palavra*) malloc(sizeof(Palavra));
	strcpy(p->pal,s);
	p->ocorr=0;
	p->prox=tab[h];
	tab[h]=p;
	return p;
}

static int conta_elems(Hash tab){
	int i;
	Palavra *p;
	int total=0;
	for(i=0;i<NTAB;i++){
		for(p=tab[i];p!=NULL;p=p->prox)
			total++;
	}
	return total;
}

static Palavra** cria_vetor(int n,Hash tab){
	int i,j=0;
	Palavra* p;
	Palavra** vet=(Palavra**) malloc(n*sizeof(Palavra));
	for(i=0;i<NTAB;i++){
		for(p=tab[i];p!=NULL;p=p->prox)
			vet[j++]=p;
	}
	return vet;
}
static int compara(const void* v1, const void* v2){
	Palavra** p1=(Palavra**)v1;
	Palavra** p2=(Palavra**)v2;
	if((*p1)->ocorr > (*p2)->ocorr) return 1;
	else if((*p1)->ocorr < (*p2)->ocorr) return 1;
	else return strcmp((*p1)->pal,(*p2)->pal);
}
static void imprime(Hash tab){
	int i;
	int n;
	Palavra **vet;
	n=conta_elems(tab);
	vet=cria_vetor(n,tab);
	qsort(vet,n,sizeof(Palavra*),compara);
	for(i=0;i<n;i++)
		printf("%s=%d\n",vet[i]->pal,vet[i]->ocorr);
	free(vet);
}
static void imprimeOcorr(Hash tab, int num){
	int i;
	int n;
	int contador=0;
	Palavra **vet;
	n=conta_elems(tab);
	vet=cria_vetor(n,tab);
	qsort(vet,n,sizeof(Palavra*),compara);
	for(i=0;i<n;i++)
		if(vet[i]->ocorr==num){
			contador++;
			printf("%s=%d\n",vet[i]->pal,vet[i]->ocorr);
		}
	if(contador==0) printf("NENHUMA OCORRENCIA COM %d REPETICOES",num);
	else printf("%d OCORRENCIAS COM %d REPETICOES",contador,num);
	free(vet);
}
int main(void){
	FILE* fp;
	Hash tab;
	char s[NPAL];
	fp=fopen("texto.txt","rt");
	if(fp==NULL) {
		printf("ERRO na abertura do arquivo.\n");
		return 0;
	}
	inicializa(tab);
	while(le_palavra(fp,s)){
		Palavra*p=acessa(tab,s);
		p->ocorr++;
	}
	//imprime(tab);	
	imprimeOcorr(tab, 10);
	return 0;
}

