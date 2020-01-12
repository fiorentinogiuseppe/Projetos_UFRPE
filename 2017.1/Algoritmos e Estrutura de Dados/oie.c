#include <stdio.h>
#include <stdlib.h>
#include <locale.h>
#include <string.h>
#define TAM 1021
#define MAX 10000

typedef char *string;



typedef struct {
   string chave;
   int    ocorrencias;
} tipoObjeto;

/*typedef struct { //estrutura palavra
   char chave[101]; //palavra
   int ocorrencias; //quantidade de vezes que a palavra se repete
}tipoObjeto;
*/

// Definio de um n das listas de colises.
typedef struct STnode *link;
struct STnode {
   tipoObjeto obj;
   link       next;
} ;

char stringnada[1];
tipoObjeto objetonulo;
tipoObjeto auxiliar[200];

void init(){
    stringnada[0] = '\0';
    objetonulo.chave = stringnada;
}







// A tabela tab[0..M-1] apontar para as M listas de colises.
link tab[TAM];

// Funo de espalhamento: transforma uma chave no vazia v em um
// nmero no intervalo 0..M-1.
//
int hash(string v, int M) {
   int i, h = v[0];
   for (i = 1; v[i] != '\0'; i++)
      h = (h * 251 + v[i]) % M;
   return h;
}
// Inicializa uma tabela que apontar as M listas de colises.
//
void STinit() {
   int h;
   for (h = 0; h < TAM; h++)
      tab[h] = NULL;
}

// Se o objeto obj j est na tabela de s�mbolos, a funo
// insert incrementa o campo ocorrencias de obj. Seno,
// obj  inserido e seu contador  inicializado com 1.
//
void STinsert(tipoObjeto obj){
   string v = obj.chave;
   int h = hash(v, TAM);
   link t = tab[h];
   for (t = tab[h]; t != NULL; t = t->next)
      if (strcmp(t->obj.chave, v) == 0) break;
   if (t != NULL)
      t->obj.ocorrencias++;
   else {
      obj.ocorrencias = 1;
      link novo = malloc(sizeof (struct STnode));
      novo->obj = obj;
      novo->next = tab[h];
      tab[h] = novo;
   }
}

// A funo search devolve um objeto obj que tenha chave v.
// Se tal objeto no existe, a funo devolve um objeto cuja
// chave  a string vazia (ou seja, chave[0] == '\0').
//
tipoObjeto STsearch(string v){
   link t;
   int h = hash(v, TAM);
   for (t = tab[h]; t != NULL; t = t->next)
      if (strcmp(t->obj.chave, v) == 0) break;
   if (t != NULL) return t->obj;
   return objetonulo;
}


int nextToken(char *content, char **point, char delimiter) {
    char tokenBuffer[TAM];
    static int lastIndex;
    int tokenSize  = 0;
    if(*(content + lastIndex) != ' ')
        tokenBuffer[tokenSize++] = *(content + lastIndex);
    while(*(content + (++lastIndex)) != '\0') {
        if(*(content + (lastIndex)) == ' ' || *(content + (lastIndex)) == ',' || *(content + (lastIndex)) == '.'){
            tokenBuffer[tokenSize] = '\0';
            *point = tokenBuffer;
            ++lastIndex; // aponta para o proximo caracter depois do espaco em branco
            return 1;
        }

        tokenBuffer[tokenSize++] = *(content + lastIndex);
    }
    return 0;
}

/*void listarPalavra(string p){

    int i, j, a, e;
    for(i=0;i<TAM;i++){
        e=0;
        for(j=0;j<200;j++){
            if(strcmp(tab[i]->obj.chave, palavras[j].word) == 0){
               e++;
            }

        }
        if(e==0){
            strcpy(palavras[i].word, tab[i]->obj.chave);
        }
    }




}*/


//char texto[MAX];

int main(){
    init();
    STinit();
    setlocale(LC_ALL, "Portuguese");
    int i, x, j;
    char *token;
    char *token2;
    FILE *f = fopen("texto2.txt", "rt");
    fseek(f, 0, SEEK_END);
    long fsize = ftell(f);
    fseek(f, 0, SEEK_SET);  //same as rewind(f);
    char *text = malloc(fsize + 1);
    char *text2 = malloc(fsize + 1);
    fread(text, fsize, 1, f);
    fread(text2, fsize, 1, f);
    fclose(f);
    text[fsize] = 0;
    int e, c = 0;
    while(nextToken(text, &token, ' ')) {
        e=0;
        tipoObjeto palavra;
        palavra.chave = token;
        STinsert(palavra);
        //printf("%s - %s\n", token, palavra.chave);
    }
    for(i=0;i<TAM;i++){
        if(tab[i] != NULL){
                printf("| %s |", tab[i]->obj.chave);
        }
    }


    //listarPalavra();


    /*do{
        char *palavra;
        for(i=0;i<fsize; i++){
            if(strcasecmp(text[c], " ") == 0){
                printf("%s", palavra);
                c++;
                break;
            }
            palavra[i] = text[c];
            c++;
        }

    }while(text[c]!='\0');*/
    printf(".");
    printf(".");
    printf(".");
    printf(".");
    printf(".");



    return 0;
}
