#include<stdio.h>
#include<stdlib.h>

main()
{
	int A[5], B[5], C[5];
    int i,j, resp;

	//Recebimento de Dados
	
        for(j=0;j<5;j++)
        {
        	printf("Digite o Valor do vetor A[%i]: ",j);
	        scanf("%i", &resp);
			A[j]=resp;
            
        }

    
 
        for(j=0;j<5;j++)
        {
        	printf("Digite o Valor da matriz B[%i]: ",j);
	        scanf("%i", &resp);
			B[j]=resp;
            
        }
        system("cls");
        

	//Print
   
        for(j=0;j<5;j++)
        {
            printf("%i\t", A[j]);
        }
        printf("\n");
  
    printf("\n");
    printf("\+");
    printf("\n");
    printf("\n");
	
	 for(j=0;j<5;j++)
        {
            printf("%i\t", B[j]);
        }

    printf("\n");
    printf("\=");
    printf("\n");
    printf("\n");
  
        for(j=0;j<5;j++)
        {
            C[j]=A[j]+B[j];
            printf("%i\t", C[j]);

        }

}
