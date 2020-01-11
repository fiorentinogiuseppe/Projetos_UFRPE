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
