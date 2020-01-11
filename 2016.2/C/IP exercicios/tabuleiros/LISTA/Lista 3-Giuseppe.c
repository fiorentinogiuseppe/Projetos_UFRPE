/*
1
main()
{
    int i,j;

    for(i=0;i<=2;i++)
    {
        for(j=0;j<=2; j++)
        {
            printf("%i-%i\t", i,j);
        }
        printf("\n");

    }
}
2
main()
{
    int i,j;

    for(i=0;i<=2;i++)
    {
        for(j=0;j<=2; j++)
        {
            if(j==i)
            {
                printf("%i-%i\t", i,j);
            }
            else
            {
                printf("\t");
            }
        }
        printf("\n");

    }
}
3
main()
{
    int i,j,y=0;

    for(i=0;i<=2;i++)
    {
        for(j=0;j<y; j++)
        {
            if(j==i)
            {
                printf("\t");
            }
            else
            {
                printf("%i-%i\t", i,j);

            }
        }
        printf("\n");
        y++;

    }
}
4
#include<stdio.h>
#include<stdlib.h>
#include<math.h>

int main()
{
    int i=0;
    int idade;
    char s;
    int contador=0;

    while(i<20)
    {

        i++;
        printf("Digite sua idade: ");
        scanf("%i", &idade);
        getchar();
        printf("Digite seu sexo: [m/f]: ");
        scanf("%c", &s);

        if(s=='m' && idade>=21)
        {
            contador++;
            system("cls");
            printf("Pessoa %i tem mais de 21 anos e eh masculino.\n\n\n", contador);
        }

        system("pause");
        system("cls");

    }
}

5
main()
{
    int LS, incremento, variavel_controladora;

    printf("Digite o Limite superior: ");
    scanf("%i", &LS);
    printf("Digite o incremento: ");
    scanf("%i", &incremento);

    for(variavel_controladora=0; variavel_controladora<=LS; variavel_controladora=variavel_controladora+incremento)
    {
        printf("%i ", variavel_controladora);
    }
}
6
main()
{
    int m,li, ls,i,j;

    printf("Digite o Limite Inferior: ");
    scanf("%i", &li);
    printf("Digite o Limite Superior: ");
    scanf("%i", &ls);
    printf("Digite qual multiplo aparecerá na tela: ");
    scanf("%i", &m);
    system ("cls");

	for(i=0; i<ls;i++)
	{
		j=i*m;
		if(j>li && j<ls)
		{
			printf("%i\n",j);
		}
	}

}
7
#include<stdio.h>
#include<stdlib.h>

int main()
{
    int n,i,val;
    printf("Digite o numero N de entradas: ");
    scanf("%i",&n);

    system("cls");

    for(i=1; i<=n; i++)
    {
        printf("Digite o %i.o de N: ", i);
        scanf("%i",&val);

        printf("O seu triplo eh: %i\n\n", val*3);

        system("pause");
        system("cls");
    }
}
8
    #include<stdio.h>
    #include<stdlib.h>
    
	main()
    {

        int i, numeros, valor, big, less, cte;
        printf("Quantos numeros voce deseja digitar?: ");
        scanf("%i",&numeros);
        
        printf("oi meu chapa!! Digite o valor:");
		scanf("%i", &valor);

		i=0;
		big=valor;
		less=valor;
		for(i=1;i<numeros;i++)
		{
			printf("oi meu chapa!! Digite o valor:");
			scanf("%i", &valor);
		
                    if(valor>big)
                    {
                        big=valor;
                    }
                    else if(valor<big)
                    {
                            less=valor;

                    }

		}
		

		printf("o maior numero eh:%i\nO menor numero eh: %i.", big, less);
    }
9
#include<stdio.h>
#include<stdlib.h>
#include<math.h>

int main()
{
    int v1,v2,i,v1_2,v2_2,v3;

    printf("Digite os 2 termos iniciais da serie de FETUCCINE: ");
    scanf("%i %i", &v1, &v2);

        v1_2=v1;
        v2_2=v2;
        printf("%i\n%i\n", v1_2, v2_2);
    for(i=3; i<=10;i++)
    {

        if(i%2!=0)
        {
            v3=v1_2+v2_2;
            v1_2= v2_2;
            v2_2=v3;
            printf("%i\n", v3);
        }
        else if(i%2==0)
        {
            v3=v2_2-v1_2;
            v1_2= v2_2;
            v2_2=v3;
            printf("%i\n", v3);

        }
    }
}
10

#include<stdio.h>
#include<stdlib.h>

int main()
{
    printf("-------------------\n");
    printf("Ola!\nNos da emissora XYZ\nestamos interessados\nem saber sua opniao\na respeito do filme\nLORD OF THE RINGS.\n");
    printf("-------------------\n");
    system("pause");
    system("cls");

    int idade, nota,i, regular=0, excelente=0, media_i=0, media_excelente, contador_tot=0;
    float porcentagem;
    float caso_2=0;
    for(i=0;i<5;i++)
    {

        printf("-------------------\n");
        printf("Legenda:\n");
        printf("[1]Regular;\n");
        printf("[2]Bom;\n");
        printf("[3]Excelente;\n");
        printf("-------------------\n");
        printf("Digite sua idade: ");
        scanf("%i", &idade);
        printf("Digite sua nota:");
        scanf("%i", &nota);
        printf("\n\n");
        system("pause");
        system("cls");


        switch (nota)
        {
            case  1:
            regular++;

            break;
            case  2:
            caso_2++;

            break;
            case  3:
            excelente++;
            media_i=media_i+excelente;

            break;
            default:
            printf("Error!!\n");
            printf("Por favor digite um valor entre 1 e 3.\n\n\n\n");
            system("pause");
            system("cls");

        }
    }
    porcentagem= (caso_2*100)/i;
    printf("\nNumero de notas regulares foram:  %i", regular);
    printf("\nA porcentagem de bons foi: %.2f\%",porcentagem);
    
    if(excelente>0)
    {
    	media_excelente=media_i/excelente;
        printf("\nA Media de pessoras que deram excelentes foram: %i", media_excelente);
	}
	else
	{
		printf("\nA Media de pessoras que deram excelentes foram: 0");
	}



}


11
int main()
{
    int num;

    do
    {
        printf("Digite um numero:");
        scanf("%i", &num);
        printf("O triplo desse numero eh: %i\n", num*3);
    }
    while(num!=0);
}
12
main()
{
    int valor, contagem;
    int i=0;
    char resp;


    do
    {
        printf("Digite um valor: ");
        scanf("%i", &valor);
        getchar();

        if(valor>=0)
        {
            i++;
        }


        printf("Deseja sair? [S/N]");
        scanf("%c", &resp);

    }
    while(resp!='s');

    printf("%i", i);



}
13
main()
{
    int valor, tot;
    float media;
    float contagem=0;
    int i=0;
    char resp;

    printf("O total de numeros sera?: ");
    scanf("%i", &tot);

    do
    {
        i++;
        printf("Digite um valor: ");
        scanf("%i", &valor);
        if(valor>=0)
        contagem=contagem+valor;
    }
    while(i<tot);
    media=contagem/tot;
    printf("%.2f", media);



}
14
#include<stdio.h>
#include<stdlib.h>


int main()
{
    int resp;

    do
    {
    printf("BOAS VINDAS\n");
    printf("1- Ola\n");
    printf("2- Bem vindo\n");
    printf("3- Sair do algoritimo\n");
    scanf("%i", &resp);

    switch (resp)
    {
    case 1:
    {
        printf("Ola\n");
        break;
    }
    case 2:
    {
        printf("Bemvindo\n");
        break;
    }
    case 3:
    {
        printf("Saindo\n");
        break;
    }
    default:
    {
    printf("valor errado\n");
    }

    }
    system("pause");
    system("cls");
    }
    while(resp!=3);
}
15
#include<stdio.h>
#include<stdlib.h>

main()
{
    int i,j,i_1,y=1, x=0;

    printf("a)\n");
    for(i=0;i<=9;i++)
    {
        for(j=0;j<y; j++)
        {
                printf("*");
        }
        printf("\n");
        y++;
    }
    system("pause");
    system("cls");
    system("pause");

    printf("b)\n");
    for(i=9;i>=0;i--)
    {
        for(j=y;j>0; j--)
        {
                printf("*");
        }
        printf("\n");
        y--;
    }
    system("pause");
    system("cls");
    system("pause");

     printf("c)\n");

     for(i=0; i<9; i++)
        {
            for(j=9; j>x; j--)
            {
                printf("*");
            }
            x++;
            printf("\n");
            for(i_1=0; i_1<y; i_1++)
            {
                printf(" ");
            }
            y++;
        }
        system("pause");
    system("cls");
    system("pause");

     printf("d)\n");

     for(i=9; i>0; i--)
        {
            for(j=x; j<9; j++)
            {
                printf("*");
            }
            x--;
            printf("\n");
            for(i_1=y; i_1>0; i_1--)
            {
                printf(" ");
            }
            y--;
        }

}
16
#include<stdio.h>
#include<stdlib.h>

main()
{
        int resp, classe, itens, hora,extra, salario_final, vendas;
        float fim;
        char resp_2;

        do
        {
            printf("---------------------------------");
            printf("\n      Bem Vindo a empresa JJJ\n");
            printf("---------------------------------");
            printf("\nEscolha o que voce deseja fazer: ");
            printf("\n[1] calcular seu salario.\n");
            printf("[2] sair.\n");
            printf("---------------------------------\n");
            scanf("%i", &resp);
            do
            {
              if(resp==1)
                {
                    system("cls");
                    printf("----------------------------------");
                    printf("\nEscolha a sua classe de trabalho: ");
                    printf("\n[1] Gerente.");
                    printf("\n[2] Horista.");
                    printf("\n[3] Trabalhador de comissao.");
                    printf("\n[4] Tarefeiro.");
                    printf("\n[5] Sair.\n");
                    printf("---------------------------------\n");
                    scanf("%i", &classe);

                    switch(classe)
                    {
                        case 1:
                            system("cls");
                            printf("Seu salario eh: R$ 2000.00\n\n\n");
                            system("pause");
                            system("cls");
                            getchar();
                            printf("Deseja fazer uma nova consulta?: [S/N] ");
                            scanf("%c", &resp_2);
                            system("pause");
                            system("cls");
                        break;
                        case 2:
                            system("cls");
                            printf("Digite a quantidade de de horas trabalhadas por voce: ");
                            scanf("%i", &hora);
                            system("pause");
                            system("cls");
                            if(hora<=40)
                            {
                                printf("Seu salario eh de: R$800.00\n\n\n");
                            }
                            else
                            {
                                extra= hora-40;
                                salario_final= (extra*(1.5*800)+800);
                                printf("Seu salario eh de: R$%i.00\n\n\n", salario_final);
                            }
                            system("pause");
                            system("cls");
                            getchar();
                            printf("Deseja fazer uma nova consulta?: [S/N] ");
                            scanf("%c", &resp_2);
                            system("pause");
                            system("cls");

                        break;
                        case 3:
                            system("cls");
                            printf("Digite a quantidade de vendas que voce realisou: ");
                            scanf("%i", &vendas);
                            fim= (((vendas*(610*5.7))/100)+610);
                            system("pause");
                            system("cls");
                            printf("Seu salario eh de: R$%.2f\n\n\n", fim);
                            system("pause");
                            system("cls");
                            getchar();
                            printf("Deseja fazer uma nova consulta?: [S/N] ");
                            scanf("%c", &resp_2);
                            system("pause");
                            system("cls");


                        break;
                        case 4:
                            system("cls");
                            printf("Digite a quantidade de itens produzidos por voce: ");
                            scanf("%i", &itens);
                            system("pause");
                            system("cls");
                            printf("Seu salario eh de: R$%i.00\n\n\n",itens*1000);
                            system("pause");
                            system("cls");
                            getchar();
                            printf("Deseja fazer uma nova consulta?: [S/N] ");
                            scanf("%c", &resp_2);
                            system("pause");
                            system("cls");
                        break;
                        case 5:
                        	exit(0);
                        	break;
                       
                        
                    }
                }
                else if( resp==2)
                {
                	exit(0);
				}
			

            }
            while(resp_2!='n');


        }
        while(resp!=2);
}
*/

