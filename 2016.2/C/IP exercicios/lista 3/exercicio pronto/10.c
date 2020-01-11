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

