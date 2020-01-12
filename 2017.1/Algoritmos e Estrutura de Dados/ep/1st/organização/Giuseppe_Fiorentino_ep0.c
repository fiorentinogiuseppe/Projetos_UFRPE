//*******************************************************************************************************************
//Universidade Federal Rural de Pernambuco - Campus Recife
//Disciplina......: Algoritmo e Estrutura de Dados
//Bacharel em Ciência da Computação
//Aluno...........: Giuseppe Fiorentino Neto
//Data............: 18/05/2017
//Numero do EP....: 0
//*******************************************************************************************************************
//*******************************************************************************************************************
//CONSIDERAÇÕES INICIAIS
//Devido ao fato de que o vetor possui uma contagem de indexação iniciada do zero e vai até n-1, onde n é o numero
//total de casas do vetor,algumas variaveis que indicam a inicialização do vetor foram inicializadas com -1(tais como
//primeira, e quarta questao). Porém outras foram iniciadas com 0( tais como segunda e terceira).
//INDICACOES DO QUE SAO AS VARIAVEIS:
//- e,i= inicio do vetor;
//- d,f= fim do vetor;
//- n= total de elementos;
//- m= meio do vetor;
//- x= inteiro qualquer;
//- v= vetor unidimensional;
//- contadorIntera= contador para realizar contagens de repetição dos loops iterativos;
//- contadorRecu= contador para realizar contagens de repetição da funções recursivas;
//ALGUNS DADOS:
//Na terceira questão foi levada em conta algumas informações:
//-A exponenciação foi estudada como se fosse um vetor de 2, mas isso foi feito apenas em relação a indexação.Por isso
//f=10 é o mesmo que 2^10;
//-expoente(f)>0.
//FORMA COMO FOI SALVO OS ARQUIVOS:
//CREDITOS:
//O algoritmo iterativo usado na quarta questao foi o mesmo feito em sala.
//**********************************************************************************************************************


#include<stdio.h>
#include<stdlib.h>
#include<stdbool.h>
int contadorRecu=0,contadorIntera=0;
//1
int maiorRecu(int e,int n,int v[]){//O algoritmo recursivo que busca encontrar o máximo de um vetor de inteiros atraves
//da divisao e conquista
    contadorRecu++;
    int m, d = n;
    if( e==d-1 ) return  v[e];// condição do caso base
    m=(e+d)/2;//momento em qua ocorre a divisao do vetor ao meio

    int x=maiorRecu(e,m,v);//chamada recursiva para  primeira metade
    int y=maiorRecu(m,d,v);// chamada recursiva para a segunda metade

    if(x>y) return x; //condicao parada
    else return y;

}

int maiorItera(int n,int v[]){//O algoritmo iterativo que busca encontrar o máximo de um vetor de inteiros atraves da
//divisao e conquista
    int maior=v[0];
    for(int i=1;i<n;i++){//for é usado para navegar no vetor
        contadorIntera++;
        if(v[i]>maior)  maior=v[i];// comparacao feita para descobrir o maior
    }
    return maior;
}

//consideração da busca pelo maximo de um vetor de inteiros:
//o algoritmo iterativo foi crescendo mais lento e o numero de chamadas foi, para os dados usados, pares
//o algoritmo recursivo cresce mais rapido e o numero de chamadas foi, para os dados usados, impares

//2
int ordemrecu(int e,int n,int v[]){// algoritmo recursivo para testar se um vetor de inteiros está em ordem cresente
// o algortimo retorna um inteiro sendo apenas 0 e 1. Onde 0 respresenta False e 1 Verdadeiro.
    contadorRecu++;
    int m, d=n;
    if(e==d-1){//caso base
        if(v[e]<=v[d])  return 1;//como o caso base poderia possuir 2 valores, 0 ou 1, foi usado esse if pra poder
                                //informar o retorno do caso base
        else return 0; }
    m=(e+d)/2;
    int x=ordemrecu(e,m,v);//chamada recursiva para a primeira metade
    int y=ordemrecu(m,d,v);//chamada recursiva para a segunda metade
    if(x==1 && y==1) return 1;//Se as duas metades sao verdadeira entao ele retornara 1, assim como na logica booleana
    else return 0;
}
int ordemitera(int n,int v[]){// algoritmo iterativo para testar se um vetor de inteiros está em ordem cresente
    int contador=0;
    for(int i=1;i<n;i++){//for é usado para navegar no vetor
        contadorIntera++;
        if(v[i]<v[i-1]) contador++;//ha uma comparacao para saber se o dado posterior eh maior que o anterior.
                                   // Como tem que ser em ordem crescente entao para que esteja assim o if nunca
                                   //deve ser acessado.
    }
    if(contador==0) return 1;// assim como no recursivo o retorno sera um inteiro que indicara se o vetor esta ou
                            //nao ordenado.
    else return 0;
}
//consideração de teste de um vetor de inteiros se está em ordem cresente ou nao
//foram feitos teste, nos dois algoritmos, de acordo com a seguinte possibilidade:
//vetores com elementos iguais
//vetores em ordem crescente: com valores negativos e positivos
//vetores em ordem decrescente: com valores negativos e positivos
//algoritmo iterativo cresce muito mais rapido que o algortimo iterativo

//3

int potenciaRecu(int base,int e,int d){//calcular recursivamente 2^n
    contadorRecu++;
//Foi pensado o algoritmo assim como eh feito com o vetor, ou seja, 2^n é como se fosse um vetor de tamanho n onde
//todos os valores sao 2. E assim como nos outros algoritmos que a manipulação dos calulos eh feito com a indexação
//do vetor é usada a mesma logica pra calcular 2^n

    if( e>d-1 || e==d-1 ) return base;//caso base

    int m=(d+e)/2;//meio

    int x=potenciaRecu(base,e,m);// calcula a metade de n
    int y=potenciaRecu(base,m,d);//calcula a outra metade de n

    int valor=x*y; // eh feito um produto dos retornos
    return valor;

}
int potenciaIte(int b,int e){//calcular iterativamente 2^n

    int questao,produto=1;
    for(int i=0;i<e;i++){//for usado pra repetir n vezes
        contadorIntera++;
        produto*=b;//produto é usado pra guardar o produto entre 2 n vezes
    }
    return produto;

}

//consideração do calculo de 2^n
//Diferente dos outros algoritmos recursivos a diferença entre recursivo e iterativo mantiveram-se nao tao altas qnto os
//anteriores.
//Os algoritmos Recursivos e Iterativos cresceram em numeros impares

//4

int buscaItera (int x, int n, int v[]) {//Mesmo algoritmo usado na aula.
   int e= -1, m, d= n;
   while (e < d-1) {
      contadorIntera++;
      m = (e + d)/2;
      if (v[m] < x) e = m;
      else d = m;
   }
   return d;
}

int buscaRecu(int x, int n,int e, int v[]){//algortimo recursivo para enontrar a posição de um inteiro x em um vetor
                                            //cresente de inteiros
    contadorRecu++;
    int d=n,k,m=(e+d)/2;
    if(e==d-1) return d; //caso base

    if(v[m]<x) return k=buscaRecu( x,  n, m,  v);//comparação para saber se o dado do vetor era maior ou menor que o x.
                                                //Pois era desconsiderado os dados em que nao fossem usados e o vetor a
                                                //cada iteracao iria diminuindo  cada vez mais.
    else return k=buscaRecu( x,  m, e,  v);
}
// A diferença entre os algoritmos foram poucas chagando a manter-se os mesmos, de acordo com os dados usados, o tempo
// todo.


void arquivo(int n,char *questao){

    int diferenca=contadorIntera-contadorRecu;
    if(diferenca<0) diferenca=diferenca*(-1);

    FILE *arq;
    arq=fopen(questao,"r");
    if(arq==NULL){
        arq=fopen(questao,"w");
	fprintf(arq,"#Iterativo-#Recursivo-Diferenca-#Tamanho;\n%i		%i	%i	%i",contadorIntera,contadorRecu,diferenca,n);
    }
    else{
	arq=fopen(questao,"a");
    	fprintf(arq,"\n%i		%i	%i	%i",contadorIntera,contadorRecu,diferenca,n);
    }

}

int main(void){// possui os dados bases e as chamadas das funcoes
    int n,e,base,f,x,i;
    int v[]={50, 60, 70,80,90,200,300,400,500};//vetor usado para todas as funcoes que nescessitam de vetores
    //1st
    n=9;
    e=-1;
    contadorRecu=0;
    contadorIntera=0;
    int respIte1=maiorItera (n, v);
    printf("O maior eh: %i\n",respIte1);

    int respRecu1=maiorRecu(e,n,v);
    printf("O maior eh: %i\n",respRecu1);

    //Arquivos
    char *questao;
    questao="Questao1.txt";
    arquivo(n,questao);

    //2nd
    n=9;
    e=0;
    contadorRecu=0;
    contadorIntera=0;
    int respIte2=ordemitera (n, v);
    printf("Ordem Iterativamente. Esta em ordem?[0=F/1=V]: %i\n",respIte2);

    int respRecu2=ordemrecu(e,n,v);
    printf("Ordem Recursivamente. Esta em ordem?[0=F/1=V]: %i\n",respRecu2);

    //Arquivos
    questao="Questao2.txt";
    arquivo(n,questao);

    //3rd
    base=2;
    int expoente=11;
    i=0;
    questao=0;
    contadorRecu=0;
    contadorIntera=0;

    int respRecu3=potenciaRecu(base,i,expoente);
    printf("Potencia Recursiva: %i\n",respRecu3);

    int respIte3=potenciaIte(base,expoente);
    printf("Potencia Iterativa: %i\n",respIte3);

    //Arquivos
    questao="Questao3.txt";
    arquivo(n,questao);


    //4th
    n=9;
    e=-1;
    x=100;
    contadorRecu=0;
    contadorIntera=0;

    int respIte4=buscaItera (x, n, v);
    printf("Busca Binaria Iterativa: %i\n",respIte4);

    int respRecu4=buscaRecu(x,n,e,v);
    printf("Busca Binaria Recursiva: %i\n",respRecu4);

    //Arquivos
    questao="Questao4.txt";
    arquivo(n,questao);



    return 0;
}

//*******************************************************************************************************************
//CONSIDERAÇÕES FINAIS
//Os Algoritmos Recursivos geralmente foram mais curto, um pouco mais lento, e com maior legibilidade que os algoritmos
//iterativos, que se mostraram mais rapido.
//*********************************************************************************************************************
