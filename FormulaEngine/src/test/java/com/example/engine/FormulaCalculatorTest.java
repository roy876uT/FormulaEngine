package com.example.engine;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FormulaCalculatorTest {

	FormulaCalculator formulaCalculator = new FormulaCalculator();
	@Test
	public void formulaTest(){
		String formula = "2*(5+5*2)/3.3+(6/2+8.2)";
		var answer = formulaCalculator.calculate(formula);
		System.out.println(formula + " =");
		System.out.println(answer);
	}

	@Test
    public void f2(){
    
    }

	@Test
    public void f3(){
    
    }



}