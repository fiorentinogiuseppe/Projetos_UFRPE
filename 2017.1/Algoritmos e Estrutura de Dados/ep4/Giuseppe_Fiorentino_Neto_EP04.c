//*******************************************************************************************************************
//Universidade Federal Rural de Pernambuco - Campus Recife
//Disciplina......: Algoritmo e Estrutura de Dados
//Bacharel em Ciência da Computação
//Aluno...........: Giuseppe Fiorentino Neto
//Data............: 04/08/2017
//Numero do EP....: 4
//*******************************************************************************************************************
//CONSIDERAÇÕES INICIAIS:
//Pelo motivo de que o EP gire em torno da busca de ocorrencia 
//de palavras em um texto foi utilizado a tabela de dispersão
//que possui complexidade, no melhor caso, constante e igual a 
//O(1) e isso independe do numero de chaves armazenadas na
//tabela
//FUNCIONAMENTO:
//A questao pede a implementação de um algoritmo que exibi o numero de vezes cada palavra ocorre em um dado texto. A forma implementada aque usa a 
//estrutura de dados Hash para deixar o algoritmo mais eficiente e mais agil. 
//Deve-se entender a priori que uma palavra é uma seqüência de uma ou mais letras (maiúsculas ou minúsculas) e dessa forma foi criado um tipo que 
//representa cada palavra. O tipo chama-se “Palavra” e possui um contador de ocorrencia , a colisão com listas e um vetor que representa o tamanho da 
//palavra. Como na língua portuguesa a maior palavra possui 46 letras( segundo o site https://www.normaculta.com.br/maior-palavra-da-lingua-portuguesa/
//) defini-se como padrão de tamanho de uma palavra 67 letras dando uma margem de erro segura.
//Para usar a tabela hash criou-se um vetor de palavras,que possui o tamanho maximo de 127 palavras, para armazenar cada palavra que foi encontrada no 
//texto.
//O pre-processameto é feito pela leitura de palavras. Captura-se uma seqüência de letras do arquivo texto, ou seja uma palavra, pulando os caracteres 
//que não são letras e armazenando a seqüência de letras a partir da posição do cursor do arquivo.
//O processo de armazenamento das palavras lidas e da sua freqüência na tabela hash é por meio de uma tabela de dispersão( que mapeia a chave de busca 
//em um índice da tabela, que é soma os códigos dos caracteres que compõem a cadeia e tira o módulo dessa soma para se obter o índice da tabela) e a 
//própria palavra como chave de busca. Assim, a partir de uma função para obter a freqüência das palavras, dada uma palavra tenta-se encontrá-la na 
//tabela e se não existir é armazenada a palavra na tabela e se existir, incremente o número de ocorrências da palavra. Isso é feito a partir de uma 
//função para acessar os elementos na tabela. Dada uma palavra (chave de busca) é retornado o ponteiro da estrutura Palavra associada. Se a palavra 
//ainda não existir na tabela, crie uma nova palavra e é forneçido como retorno essa nova palavra criada.
//Para finalizar o que foi pedido na questao foi criada uma função para exibir as ocorrências em ordem decrescente. Foi criado um vetor armazenando as 
//palavras da tabela de dispersão e ordenando o vetor para facilitar a vizualização quando exibir o conteúdo do vetor. Além disso também foi criada 
//uma função, baseada na função anterior, que mostra apenas as palavras para um dado numero de ocorrencias que o usuario que define.


#include<stdio.h>
#include<stdlib.h>
#include<ctype.h>//biblioteca usado para usar a função isalpha
#include<string.h>
#define NPAL 64//Dimensão máxima de cada palavra
#define NTAB 127//Dimensão máxima da tabela de dispersão

//Tipo que representa cada palavra
struct palavra{
	char pal[NPAL];
	int ocorr;
	struct palavra* prox;
};
//tipo que represena a tabela de HASH
typedef struct palavra Palavra;

typedef Palavra* Hash[NTAB];

//captura de cada palavra no arquivo. Ela pula caracteres que não são letras. Acentando apenas letras e letras que não tenham acentos( por isso 
//palavras monossilabicas que possuem acentos passam ficar com apenas 1 letra) e armazenando a seqüência de letras a partir da posição do cursor do
//arquivo 
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
//Função para inicializar a tabela, atribuindo NULL a cada elemento
static void inicializa(Hash tab) {
   int h;
   for (h = 0; h < NTAB; h++)
      tab[h] = NULL;
}
//Função hash. É a função que mapeia a chave de busca (uma cadeia de caracteres) em um índice da tabela, que é 
//soma os códigos dos caracteres que compõem a cadeia e tira o módulo dessa soma para se obter o índice da tabela
int hash(char *s) {
   int i, total = 0;
   for (i = 0; s[i] != '\0'; i++)
      total += s[i];
   return total%NTAB;
}
//Função para acessar os elementos na tabela.É enviado uma palavra (chave de busca) e tem como retorno o ponteiro da estrutura Palavra associada.
//Se a palavra que foi entrada não existir na tabela, crie uma nova palavra e forneça como retorno essa nova palavra criada
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
//função para percorrer a tabela e contar o número de palavras
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
//função para criar dinamicamente o vetor de ponteiros. Que será usado na função imprime para cria os vetores para que eles sejam ordenados
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
//Essa é a função que é nescessario para que o qsort funcione. Essa função compara 2 elementos e retorna um inteiro que sera usado na função qsort
static int compara(const void* v1, const void* v2){
	Palavra** p1=(Palavra**)v1;
	Palavra** p2=(Palavra**)v2;
	if((*p1)->ocorr > (*p2)->ocorr) return -1;
	else if((*p1)->ocorr < (*p2)->ocorr) return 1;
	else return strcmp((*p1)->pal,(*p2)->pal);
}
//A função recebe uma tabela HASH, conta seus elementos e cria um vetor com o mesmo tamanho da tabela. Além disso a ordena usando o algoritmo, 
//que se enconta na biblioteca da linguagem c, quicksort. E por a imprime
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
////A função recebe uma tabela HASH, conta seus elementos e cria um vetor com o mesmo tamanho da tabela. Além disso a ordena usando o algoritmo, 
//que se enconta na biblioteca da linguagem c, quicksort. E imprime apenas as palavras com uma dada ocorrencia e mostra a quantidade de palavras com 
//aquela ocorrencia
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
	//cria o ponteiro para usar na leitura do arquivo
	FILE* fp;
	//cria a tabela hash
	Hash tab;
	//cria o vetor que representará cada palavra
	char s[NPAL];
	//leitura do arquivo de texto
	fp=fopen("texto.txt","rt");
	if(fp==NULL) {
		printf("ERRO na abertura do arquivo.\n");
		return 0;
	}
	//inicializa a tabela HASH
	inicializa(tab);
	//Trecho da função principal que acessa cada palavra e incrementa o seu número de ocorrências online
	while(le_palavra(fp,s)){
		Palavra*p=acessa(tab,s);
		p->ocorr++;
	}
	//imprime todas as palavras e suas ocorrencias
	imprime(tab);	
	//imprime apenas as palavras com, por exemplo, a ocorrencia 10
	imprimeOcorr(tab, 10);
	return 0;
}

