#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#define M 2
// Tamanho da tabela.

typedef char *string;
typedef struct reg{
   string chave;
   int    ocorrencias;
} tipoObjeto;

char strvazia[1];

tipoObjeto objetonulo;

// Definição de um nó das listas de colisões.
typedef struct STnode *link;
struct STnode {
   tipoObjeto obj;
   link       next;
} ;

// A tabela tab[0..M-1] apontará para as M listas de colis�es.
link tab[M];

// Função de espalhamento: transforma uma chave n�o vazia v em um
// número no intervalo 0..M-1.
//
int hash(string v, int m) {
   int i, h = v[0];
   for (i = 1; v[i] != '\0'; i++)
      h = (h * 251 + v[i]) % m;
   return h;
}


// Inicializa uma tabela que apontar� as M listas de colis�es.
//
void STinit() {
   int h;
   for (h = 0; h < M; h++)
      tab[h] = NULL;
}
// Se o objeto obj j� est� na tabela de s�mbolos, a fun��o
// insert incrementa o campo ocorrencias de obj. Sen�o,
// obj � inserido e seu contador � inicializado com 1.
//
void STinsert(tipoObjeto obj)
{
	printf("%s\n",obj.chave);
   string v = obj.chave;
   int h = hash(v, M);
   link t = tab[h];
   for (t = tab[h]; t != NULL; t = t->next)
      if (strcmp(t->obj.chave, v) == 0) break;
   if (t != NULL)
      t->obj.ocorrencias++;
   else {
      obj.ocorrencias = 1;
      link novo = malloc(sizeof (link));
      novo->obj = obj;
      novo->next = tab[h];
      tab[h] = novo;
   }
}
// A fun��o search devolve um objeto obj que tenha chave v.
// Se tal objeto n�o existe, a fun��o devolve um objeto cuja
// chave � a string vazia (ou seja, chave[0] == '\0').
//

tipoObjeto STsearch(string v)
{
   link t;
   int h = hash(v, M);
   for (t = tab[h]; t != NULL; t = t->next)
      if (strcmp(t->obj.chave, v) == 0) break;
   if (t != NULL) return t->obj;
   return objetonulo;
}
char txt[1000];
void arquivo(){
	char info[50];
	FILE *arq;
	arq=fopen("texto.txt","r");
	if(arq==NULL) printf("ERRO");
	int cont=0;
	while((fgets(info, sizeof(info), arq))!=NULL ){
		if(cont==0) strcpy(txt,info);
		else strcat(txt,info);
		cont++;
	}
	fclose(arq);
}

void main(){
	STinit();
	//iniciando variaveis globais
	strvazia[1]='\0';
	objetonulo.chave = strvazia;
	arquivo();
	//printf("%s",txt);
	/*char *text[10];
	text[0]="cooeee";
	text[1]="rapaziada";
	text[2]="coeeeeeeeee";
	text[3]="rapaziada";
	text[4]="cooeeeeeeeeeeeeeeeeeeeeeeeee";
	text[5]="rapaziada";
	text[6]="kkk";
	text[7]="rapaziada";
	text[8]="kkk";
	text[9]="cooeee";
	
	int i;
	tipoObjeto novo;
	for(i=0;i<10;i++){
		novo.chave=text[i];
		STinsert(novo);		
	}
	tipoObjeto retorno=STsearch("rapaziada");
	printf("%i",retorno.ocorrencias);
	*/
	int i,j;
	tipoObjeto novo;
	char palavra[50];
	for (i = 0 ,j =0; i<strlen(txt); i++ , j++){
		palavra[j]=txt[i];
		if(txt[i]==' ' || txt[i]==',' || txt[i]=='.'){
			printf("%s\n",palavra);
			novo.chave=palavra;
			STinsert(novo);	
			j=-1;
		}
	}	
	tipoObjeto retorno=STsearch("ae");
	printf("%i",retorno.ocorrencias);

}
