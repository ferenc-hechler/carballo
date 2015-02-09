package com.alonsoruibal.chess;

import com.alonsoruibal.chess.evaluation.Evaluator;
import com.alonsoruibal.chess.evaluation.ExperimentalEvaluator;
import com.alonsoruibal.chess.log.Logger;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ExperimentalEvaluatorTest {
	private static final Logger logger = Logger.getLogger("ExperimentalEvaluatorTest");

	ExperimentalEvaluator evaluator;

	@Before
	public void setUp() throws Exception {
		evaluator = new ExperimentalEvaluator(new Config());
		evaluator.debug = true;
	}

	@Test
	public void printAllPcsq() {
		ExperimentalEvaluator experimentalEvaluator = new ExperimentalEvaluator(new Config());
		logger.debug("***PAWN");
		printPcsq(experimentalEvaluator.pawnIndexValue);
		logger.debug("***KNIGHT");
		printPcsq(experimentalEvaluator.knightIndexValue);
		logger.debug("***BISHOP");
		printPcsq(experimentalEvaluator.bishopIndexValue);
		logger.debug("***ROOK");
		printPcsq(experimentalEvaluator.rookIndexValue);
		logger.debug("***QUEEN");
		printPcsq(experimentalEvaluator.queenIndexValue);
		logger.debug("***KING");
		printPcsq(experimentalEvaluator.kingIndexValue);
		logger.debug("PCSQ tables generated");
	}

	private static void printPcsq(int pcsq[]) {
		StringBuffer sb = new StringBuffer();
		for (int k = 0; k < 2; k++) {
			if (k == 0) {
				sb.append("Opening:\n");
			} else {
				sb.append("Endgame:\n");
			}
			for (int i = 0; i < 64; i++) {
				String aux = "     " + (k == 0 ? Evaluator.o(pcsq[i]) : Evaluator.e(pcsq[i]));
				aux = aux.substring(aux.length() - 5);
				sb.append(aux);
				if (i % 8 != 7) {
					sb.append(",");
				} else {
					sb.append("\n");
				}
			}
		}
		logger.debug(sb.toString());
	}

	public static int countSubstring(String subStr, String str) {
		return (str.length() - str.replace(subStr, "").length()) / subStr.length();
	}

	@Test
	public void testEvaluatorSimmetry1() {
		String fen = "r2q1rk1/ppp2ppp/2n2n2/1B1pp1B1/1b1PP1b1/2N2N2/PPP2PPP/R2Q1RK1 w QKqk - 0 0";
		Board board = new Board();
		board.setFen(fen);
		int value = evaluator.evaluate(board);
		System.out.println("value = " + value);
		assertEquals(ExperimentalEvaluator.TEMPO, value);
	}

	@Test
	public void testEvaluatorSimmetry2() {
		String fen = "7k/7p/6p1/3kp3/3PK3/1P6/P7/K7 w - - 0 0";
		Board board = new Board();
		board.setFen(fen);
		int value = evaluator.evaluate(board);
		System.out.println("value = " + value);
		assertEquals(ExperimentalEvaluator.TEMPO, value);
	}

	@Test
	public void testPawnClassification() {
		Board board = new Board();
		board.setFen("8/8/7p/1P2Pp1P/2Pp1PP1/8/8/7K w - - 0 0");
		evaluator.evaluate(board);
		assertEquals("Four passers", 4, countSubstring("passed ", evaluator.debugSB.toString()));
		assertEquals("One outside passed", 1, countSubstring("outside ", evaluator.debugSB.toString()));
		assertEquals("Three supported", 3, countSubstring("supported ", evaluator.debugSB.toString()));
		assertEquals("Six connected", 6, countSubstring("connected ", evaluator.debugSB.toString()));
		assertEquals("Three isolated", 3, countSubstring("isolated ", evaluator.debugSB.toString()));
		assertEquals("Four opposed", 4, countSubstring("opposed ", evaluator.debugSB.toString()));

		board.setFen("7k/p6p/PP6/6P1/8/7P/8/7K w - - 0 0");
		evaluator.evaluate(board);
		assertEquals("Two candidates", 2, countSubstring("candidate ", evaluator.debugSB.toString()));
		assertEquals("No backwards", 0, countSubstring("backwards ", evaluator.debugSB.toString()));

		board.setFen("7k/3r4/8/3p4/8/8/8/R6K w - - 0 0");
		evaluator.evaluate(board);
		assertEquals("Runner", 1, countSubstring("runner ", evaluator.debugSB.toString()));

		board.setFen("7k/8/8/3p4/8/8/1r6/R6K w - - 0 0");
		evaluator.evaluate(board);
		assertEquals("No Runner", 0, countSubstring("runner ", evaluator.debugSB.toString()));
		assertEquals("Mobile", 1, countSubstring("mobile ", evaluator.debugSB.toString()));
		assertEquals("No Outside", 0, countSubstring("outside ", evaluator.debugSB.toString()));

		board.setFen("7k/8/8/3p4/R7/8/1r6/7K w - - 0 0");
		evaluator.evaluate(board);
		assertEquals("No Runner", 0, countSubstring("runner ", evaluator.debugSB.toString()));
		assertEquals("No Mobile", 0, countSubstring("mobile ", evaluator.debugSB.toString()));

		board.setFen("7k/5ppp/8/2p5/P7/8/5PPP/7K w - - 0 0");
		assertTrue("Outside passer superior to inside passer", evaluator.evaluate(board) > 10);

		board.setFen("7k/8/8/5P2/5P2/8/8/7K w - - 0 0");
		evaluator.evaluate(board);
		assertEquals("Doubled", 1, countSubstring("doubled ", evaluator.debugSB.toString()));
		assertEquals("Not connected", 0, countSubstring("connected ", evaluator.debugSB.toString()));
		assertEquals("Only one passed", 1, countSubstring("passed ", evaluator.debugSB.toString()));

		board.setFen("R7/3p3p/8/3P2P1/3k4/1p5p/1P1NKP1P/7q w - -");
		evaluator.evaluate(board);
		assertEquals("One backwards", 1, countSubstring("backwards ", evaluator.debugSB.toString()));

		board.setFen("7k/8/8/3p1p2/1p1P1Pp1/1P2P1P1/P1P4P/7K w - -");
		evaluator.evaluate(board);
		assertEquals("Five backwards", 5, countSubstring("backwards ", evaluator.debugSB.toString()));

		board.setFen("7k/7P/8/5p2/8/8/6P1/7K w - -");
		evaluator.evaluate(board);
		assertEquals("One backwards", 1, countSubstring("backwards ", evaluator.debugSB.toString()));

		board.setFen("7k/8/Pp6/8/8/1P6/8/7K w - -");
		evaluator.evaluate(board);
		assertEquals("One backwards", 1, countSubstring("backwards ", evaluator.debugSB.toString()));

		board.setFen("7k/8/P1p5/8/8/1P6/8/7K w - -");
		evaluator.evaluate(board);
		assertEquals("One backwards", 1, countSubstring("backwards ", evaluator.debugSB.toString()));

		board.setFen("7k/2p5/P7/8/8/1P6/8/7K w - -");
		evaluator.evaluate(board);
		assertEquals("One backwards", 1, countSubstring("backwards ", evaluator.debugSB.toString()));

		board.setFen("7k/2p5/8/P7/8/1P6/8/7K w - -");
		evaluator.evaluate(board);
		assertEquals("No backwards", 0, countSubstring("backwards ", evaluator.debugSB.toString()));

		board.setFen("7k/8/8/P7/2p5/1P6/8/7K w - -");
		evaluator.evaluate(board);
		assertEquals("No backwards", 0, countSubstring("backwards ", evaluator.debugSB.toString()));

		board.setFen("7k/8/4p3/8/4pp2/8/8/7K w - -");
		evaluator.evaluate(board);
		assertEquals("One Backwards", 1, countSubstring("backwards ", evaluator.debugSB.toString()));
		assertEquals("One Doubled", 1, countSubstring("doubled ", evaluator.debugSB.toString()));

		board.setFen("7k/4p3/8/5p2/4p3/8/8/7K w - -");
		evaluator.evaluate(board);
		assertEquals("No Backwards", 0, countSubstring("backwards ", evaluator.debugSB.toString()));
		assertEquals("One Doubled", 1, countSubstring("doubled ", evaluator.debugSB.toString()));

		board.setFen("7k/4p3/8/5p2/3Pp3/8/8/7K w - -");
		evaluator.evaluate(board);
		assertEquals("One Backwards", 1, countSubstring("backwards ", evaluator.debugSB.toString()));
		assertEquals("One Doubled", 1, countSubstring("doubled ", evaluator.debugSB.toString()));

		board.setFen("7k/2P5/pp6/1P6/8/8/8/7K w - -");
		evaluator.evaluate(board);
		assertEquals("No backwards because it can capture", 0, countSubstring("backwards ", evaluator.debugSB.toString()));

	}

	@Test
	public void testPassedPawn1() {
		String fen = "7k/7p/P7/8/8/6p1/7P/7K w QKqk - 0 0";
		Board board = new Board();
		board.setFen(fen);
		int value = evaluator.evaluate(board);
		System.out.println("value = " + value);
		assertTrue(value > 0);
	}

	@Test
	public void testKnightTrapped() {
		String fen = "NPP5/PPP5/PPP5/8/8/8/8/k6K w - - 0 0";
		Board board = new Board();
		board.setFen(fen);
		int value = evaluator.evaluate(board);
		System.out.println("value = " + value);
		assertTrue(value > 0);
	}

	@Test
	public void testKingSafety() {
		String fen = "r6k/1R6/8/8/8/8/8/7K w QKqk - 0 0";
		Board board = new Board();
		board.setFen(fen);
		int value = evaluator.evaluate(board);
		System.out.println("value = " + value);
		assertTrue(value > 0);
	}

	@Test
	public void testBishopBonus() {
		String fen1 = "3BB2k/8/8/8/8/8/8/7K w QKqk - 0 0";
		String fen2 = "2B1B2k/8/8/8/8/8/8/7K w QKqk - 0 0";
		Board board = new Board();
		board.setFen(fen1);
		int value1 = evaluator.evaluate(board);
		board.setFen(fen2);
		int value2 = evaluator.evaluate(board);
		System.out.println("value1 = " + value1);
		System.out.println("value2 = " + value2);
		assertTrue(value1 >= value2 + 40);
	}
}