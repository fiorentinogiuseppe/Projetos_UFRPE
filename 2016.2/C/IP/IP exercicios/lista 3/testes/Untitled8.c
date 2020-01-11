    main()
    {
        
        int i, numeros, valor, big;
        printf("Quantos numeros voce deseja digitar?: ");
        scanf("%i",&numeros);
			
		i=0;	
		big=0;
		do
		{
			printf("oi meu chapa!! Digite o valor:");
			scanf("%i", &valor);
			i++;
			
			if(valor>big);
			{
				big= valor;
			}
		}
		while(i<numeros);
		printf("%i", big);
    }
