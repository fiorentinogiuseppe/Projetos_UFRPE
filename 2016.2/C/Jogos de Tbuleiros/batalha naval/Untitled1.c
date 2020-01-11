#include<stdio.h>
#include<stdlib.h>
#include<time.h>

main()
{
    srand(time(NULL));
    int tabuleiro[8][8];
    int i,j;

    //tabuleiro

    for(i=0;i<8;i++)
    {
        for(j=0;j<8;j++)
        {
            tabuleiro[i][j]=0;
        }
    }


    //taubleiro do jagodor 1

    //B1
    int linha1,coluna1;

        linha1=0+rand()%8;
        coluna1=0+rand()%8;
        tabuleiro[linha1][coluna1]=11;
    //B2
    for(i=0;i<8;i++)
    {
        int linha2,coluna2;
        linha2=rand()%7;
        coluna2=rand()%7;
        if(tabuleiro[linha2][coluna2]==0 && tabuleiro[linha2][coluna2+1]==0)
        {
            tabuleiro[linha2][coluna2]=121;
            tabuleiro[linha2][coluna2+1]=122;
            break;
        }
        else
        {
            tabuleiro[linha2][coluna2]=0;
            tabuleiro[linha2][coluna2+1]=0;

        }
    }


    //B3
    for(i=0;i<8;i++)
    {
        int linha2,coluna2;
        linha2=1+rand()%6;
        coluna2=1+rand()%6;
        if(tabuleiro[linha2][coluna2]==0 && tabuleiro[linha2+1][coluna2+1]==0)
        {
            tabuleiro[linha2][coluna2]=131;
            tabuleiro[linha2+1][coluna2+1]=132;
            break;
        }
        else
        {
            tabuleiro[linha2][coluna2]=0;
            tabuleiro[linha2+1][coluna2+1]=0;

        }
    }

    //B4
    for(i=0;i<8;i++)
    {
        int linha2,coluna2;
        linha2=1+rand()%6;
        coluna2=1+rand()%6;
        if(tabuleiro[linha2][coluna2]==0 && tabuleiro[linha2+1][coluna2-1]==0)
        {
            tabuleiro[linha2][coluna2]=141;
            tabuleiro[linha2+1][coluna2-1]=142;
            break;
        }
        else
        {
            tabuleiro[linha2][coluna2]=0;
            tabuleiro[linha2+1][coluna2-1]=0;

        }
    }



    //tabuleiro final
      for(i=0;i<8;i++)
    {
        printf("\t%i",i);
    }
    printf("\n\n");
      for(i=0;i<8;i++)
    {
        printf("%i\t",i);
        for(j=0;j<8;j++)
        {
          printf("%i\t", tabuleiro[i][j]);
        }
        printf("\n");
    }
    system("pause");
    system("cls");
    //jogador 2
    int tabuleiro2[8][8];
    for(i=0;i<8;i++)
    {
        for(j=0;j<8;j++)
        {
            tabuleiro2[i][j]=0;
        }
    }
    //B1
        linha1=0+rand()%8;
        coluna1=0+rand()%8;
        tabuleiro2[linha1][coluna1]=11;
    //B2
    for(i=0;i<8;i++)
    {
        int linha2,coluna2;
        linha2=rand()%7;
        coluna2=rand()%7;
        if(tabuleiro2[linha2][coluna2]==0 && tabuleiro2[linha2][coluna2+1]==0)
        {
            tabuleiro2[linha2][coluna2]=221;
            tabuleiro2[linha2][coluna2+1]=222;
            break;
        }
        else
        {
            tabuleiro2[linha2][coluna2]=0;
            tabuleiro2[linha2][coluna2+1]=0;

        }
    }


    //B3
    for(i=0;i<8;i++)
    {
        int linha2,coluna2;
        linha2=1+rand()%6;
        coluna2=1+rand()%6;
        if(tabuleiro2[linha2][coluna2]==0 && tabuleiro2[linha2+1][coluna2+1]==0)
        {
            tabuleiro2[linha2][coluna2]=231;
            tabuleiro2[linha2+1][coluna2+1]=232;
            break;
        }
        else
        {
            tabuleiro2[linha2][coluna2]=0;
            tabuleiro2[linha2+1][coluna2+1]=0;

        }
    }

    //B4
    for(i=0;i<8;i++)
    {
        int linha2,coluna2;
        linha2=1+rand()%6;
        coluna2=1+rand()%6;
        if(tabuleiro2[linha2][coluna2]==0 && tabuleiro2[linha2+1][coluna2-1]==0)
        {
            tabuleiro2[linha2][coluna2]=241;
            tabuleiro2[linha2+1][coluna2-1]=242;
            break;
        }
        else
        {
            tabuleiro2[linha2][coluna2]=0;
            tabuleiro2[linha2+1][coluna2-1]=0;

        }
    }

    //tabuleiro final do 2
      for(i=0;i<8;i++)
    {
        printf("\t%i",i);
    }
    printf("\n\n");
      for(i=0;i<8;i++)
    {
        printf("%i\t",i);
        for(j=0;j<8;j++)
        {
          printf("%i\t", tabuleiro2[i][j]);
        }
        printf("\n");
    }

    //jogo

}

