#include<stdio.h>
#include<stdlib.h>
int inserir(int i,int j)
{

    int resposta;
    printf("Digite a nota %i do aluno %i",i+1,j+1);
    scanf("%i",&resposta);
    return resposta;

}
int media(int x)
{
    int nota1[3][x];
    float mediaa[x];
    int i,j;
    for (i=0;i<3;i++)
    {
        for(j=0;j<x;j++)
        {
             nota1[i][j]=inserir(i,j);
        }
    }
    for(i=0;i<x;i++)
    {
        mediaa[i]=0;
    }
    for (i=0;i<3;i++)
    {
        for(j=0;j<x;j++)
        {
             mediaa[j]+=nota1[i][j];
        }
    }
    system("cls");
         for(j=0;j<x;j++)
        {

            printf("A media do aluno %i: %.1f\n", j+1, mediaa[j]/3);
        }

}


main()
{
    int turma;
    printf("Digite o numero de alunos: ");
    scanf("%i",&turma);
    media(turma);

}
