//2.
#include<stdio.h>
#include<stdlib.h>

main()
{
    int i, media=0, mediaturma=0;
    int nota[5];


    //nota 1
    for(i=0;i<3; i++)
    {
        printf("Digite a nota 1 do aluno %i: ", i+1);
        scanf("%i", &nota[i]);
    }

    //media da turma
    for(i=0;i<3; i++)
    {
        media+=nota[i];
        mediaturma= media/3;
    }


    //comparação
    int maior,menor;
    maior=nota[0];
    menor=nota[0];
    for(i=0;i<3;i++)
    {
            if(nota[i]>maior)
            {
                maior=nota[i];
            }
            else
            {
                menor=nota[i];
            }
    }
    //maior nota e menor nota
    printf("A maior nota eh:%i\nA menor nota eh:%i\n", maior,menor);
    printf("A media da turma eh: %i\n", mediaturma);


}
