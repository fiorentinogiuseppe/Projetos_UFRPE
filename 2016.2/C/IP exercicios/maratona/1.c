#include<stdio.h>
#include<stdlib.h>

main()
{
    //tabuleiro vazio
    int tabuleiro[3][3];
    int i,j;
    for(i=0;i<3;i++)
    {
        printf("\t%i",i);
    }
    printf("\n");
    for(i=0;i<3;i++)
    {
        for(j=0;j<3;j++)
        {
            tabuleiro[i][j]= 0;
        }
    }
     for(i=0;i<3;i++)
    {
        printf("%i\t", i);
        for(j=0;j<3;j++)
        {
            printf("%i\t", tabuleiro[i][j]);
        }
        printf("\n");
    }

    //coordenadas e tabuleiro cheio
    int x1,x2,x3,x4,s,valor, e=0;
    do
    {

        do
            { //jogador 1
            printf("Vez do 1: ");
            scanf("%i-%i", &x1,&x2);
            if(tabuleiro[x1][x2]!=0)
            {
                printf("Valor ja digitador\n");
                valor=0;
            }
            else
            {
                tabuleiro[x1][x2]=1;
                valor=1;
            }
        }
        while(valor!=1);


        system("cls");

        for(i=0;i<3;i++)
        {
        printf("\t%i",i);
        }
        printf("\n");
        for(i=0;i<3;i++)
        {
            printf("%i\t", i);
            for(j=0;j<3;j++)
            {

                printf("%i\t", tabuleiro[i][j]);
            }
            printf("\n");
        }

        //saida
        ++e;
        if(e==8)
        {
            break;
        }
        //jogador 2
           do
            {
                printf("Vez do 2: ");
                scanf("%i-%i", &x3,&x4);
                if(tabuleiro[x3][x4]!=0)
                {
                    printf("Valor ja digitador\n");
                    valor=0;
                }
                else
                {
                    tabuleiro[x3][x4]=2;
                    valor=1;
                }
            }
            while(valor==0);

            system("cls");
            for(i=0;i<3;i++)
            {
            printf("\t%i",i);
            }
            printf("\n");
            for(i=0;i<3;i++)
            {
                printf("%i\t", i);
                for(j=0;j<3;j++)
                {
                    printf("%i\t", tabuleiro[i][j]);
                }
                printf("\n");
            }

        //vitoria

        for(i=0;i<3;i++)
        {
            for(j=0;j<3;j++)
            {
				if(tabuleiro[i][0]==tabuleiro[i][1] && tabuleiro[i][0]==tabuleiro[i][2] && tabuleiro[i][0]!= 0 )
					{
					    system("cls");
                        printf("O jogador %i Venceu!!!\n\n\n",tabuleiro[i][0]);
                        s=42;
					}
					else if(tabuleiro[0][0]==tabuleiro[1][1] && tabuleiro[0][0]==tabuleiro[2][2] && tabuleiro[0][0]!=0)
					{
                        system("cls");
                        printf("O jogador %i Venceu!!!\n\n\n",tabuleiro[0][0]);
                        s=42;
					}
					else if(tabuleiro[0][2]==tabuleiro[1][1] && tabuleiro[0][2]==tabuleiro[2][0] && tabuleiro[0][2]!=0)
					{
                        system("cls");
                        printf("O jogador %i Venceu!!!\n\n\n",tabuleiro[0][2]);
                        s=42;
					}
					else  if(tabuleiro[0][j]==tabuleiro[1][j] && tabuleiro[0][j]==tabuleiro[2][j] && tabuleiro[0][j]!=0)
					{
                        system("cls");
                        printf("O jogador %i Venceu!!!\n\n\n",tabuleiro[0][j] );
                        s=42;
					}

            }

		}

    }
    while(s!=42);

    //vitoria
    if(s==8)
    {
        system("cls");
        printf("VELHA!!");
    }


}
