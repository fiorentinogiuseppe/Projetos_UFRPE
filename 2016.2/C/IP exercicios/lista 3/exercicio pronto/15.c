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

