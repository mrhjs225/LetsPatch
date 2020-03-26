package com.github.thwak.confix.pool;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.io.File;
import java.util.ArrayList;

import com.github.thwak.confix.tree.Node;
import com.github.thwak.confix.tree.TreeUtils;

import script.model.EditOp;
import tree.TreeNode;

public class TestContextIdentifier extends ContextIdentifier {

	private static final long serialVersionUID = -8352611691723991826L;
	private ArrayList<String> tempList = new ArrayList<>();

	@Override
	public Context getContext(EditOp op) {
		if(op.getType().equals(Change.INSERT)){
			StringBuffer sb = new StringBuffer();
			sb.append("P:");
			TreeNode parent = null;
			
			parent = op.getNode().getParent();
			if(parent.getType() == ASTNode.BLOCK && parent.getParent() != null)
				parent = parent.getParent();
			
			
			// // System.out.println("parent0: " + parent.getASTNode());
			// // System.out.println("parent4: " + parent.getASTNode().getAST().getClass().getDeclaredMethods());
			// // for(int i = 0; i < parent.getASTNode().getAST().getClass().getDeclaredMethods().length; i++) {
			// // 	System.out.println("	" + parent.getASTNode().getAST().getClass().getDeclaredMethods()[i]);
			// // }
			// // System.out.println("parent6: " + parent.getASTNode().getAST().getClass().getMethods());
			// // for(int i = 0; i < parent.getASTNode().getAST().getClass().getMethods().length; i++) {
			// // 	System.out.println("	" + parent.getASTNode().getAST().getClass().getMethods()[i]);
			// // }
			// // System.out.println(parent.children.size());
			// TreeNode children = null;
			// children = parent.children.get(0);
			// while(children.children.size() != 0) {
			// 	children = children.children.get(0);
			// }
			// // System.out.println("children: "+children.getASTNode());
			// // System.out.println("children name: " + children.getClass().getSimpleName());
			// // System.out.println("children name: " + children.getASTNode().get);
			// // for(int i = 0; i < children.getASTNode().getClass().getFields().length; i++) {
			// // 	// System.out.println("	" + children.getASTNode().getClass().getFields()[i]);
			// // }
			// // for(int i = 0; i < children.getASTNode().getClass().getDeclaredFields().length; i++) {
			// // 	System.out.println("	" + children.getASTNode().getClass().getFields()[i]);
			// // }
			// // System.out.println("test1: " + parent.getASTNode().getAST());
			// // parser.setsource
			
			// ASTParser parser = ASTParser.newParser(AST.JLS8);
			// parser.setKind(ASTParser.K_COMPILATION_UNIT);
			// // parser.setSource(parent.getASTNode().toString().toCharArray());
			// parser.setSource(new File("/home/hjsvm/hjsaprvm/condatabase/commitfile/collections/COLLECTIONS-214/afterCommit/ExtendedProperties.java").toString().toCharArray());
			// CompilationUnit compilationunit = (CompilationUnit) parser.createAST(null);
			// try {
			// 	compilationunit.accept(new ASTVisitor() {
			// 		@Override
			// 		public boolean visit(VariableDeclarationFragment node) {
			// 			try {
			// 				System.out.println("hello world");
			// 				System.out.println(node.toString());
			// 			} catch (Exception e1) {
			// 				System.out.println("error1:" + e1);
			// 			}
			// 			return false;
			// 		}
			// 	});
			// 	System.out.println("hello");
			// } catch (Exception e) {
			// 	System.out.println("error2:" + e);
			// }

			// try another thing....
			// System.out.println("parent: " + parent.getASTNode());
			// System.out.println("--------------------------------------------");
			// for (int i = 0; i < parent.children.size(); i++) {
			// 	System.out.println("children" + i + ":" + parent.children.get(i));
			// 	System.out.println("children" + i + " code: " + parent.children.get(i).getASTNode());
			// 	if(parent.children.get(i).children.size() != 0) {
			// 		for (int j = 0; j < parent.children.get(i).children.size(); j++) {
			// 			System.out.println("children of children" + j + ":" + parent.children.get(i).children.get(j));
			// 			System.out.println("children of children" + j + ":" + parent.children.get(i).children.get(j).getASTNode());
			// 			if (parent.children.get(i).children.get(j).children.size() != 0) {
			// 				for (int k = 0; k < parent.children.get(i).children.get(j).children.size(); k++) {
			// 					System.out.println("children of children of child" + k + ":" + parent.children.get(i).children.get(j).children.get(k));
			// 					System.out.println("children of children of childcode" + k + ":" + parent.children.get(i).children.get(j).children.get(k).getASTNode());
			// 				}
			// 			}
			// 		}
			// 	}
			// }
			// collectClass(parent);
			// for(int i = 0; i < tempList.size(); i++) {
			// 	System.out.println(tempList.get(i));
			// }


			// Node testnode = TreeUtils.getNode(children.getASTNode());
			// System.out.println("treeutils1: " + TreeUtils.computeLabel(testnode));
			// System.out.println("treeutils1: " + TreeUtils.getLabel(children.getASTNode()));
			// System.out.println("treeutils1: " + TreeUtils.getTypeName(children.getASTNode()));

			if(parent != null){
				if(parent.getMatched() != null){
					parent = parent.getMatched();
				}
				sb.append(TreeUtils.getTypeName(parent.getType()));
				StructuralPropertyDescriptor desc = null;
				ASTNode astNode = op.getNode().getASTNode();
				if(astNode.getParent().getNodeType() == ASTNode.EXPRESSION_STATEMENT)
					desc = astNode.getParent().getLocationInParent();
				else
					desc = astNode.getLocationInParent();
				if(desc != null){
					sb.append(TreeUtils.SYM_OPEN);
					sb.append(desc.getId());
					sb.append(TreeUtils.SYM_CLOSE);
				}
			}
			// System.out.println("parentCode:" + parent.getASTNode());
			TreeNode node = op.getNode();
			TreeNode left = node.getLeft();
			TreeNode right = node.getRight();
			while(right != null && !right.isMatched())
				right = right.getRight();
			//Get hash of old nodes as contexts.
			if(left != null && left.isMatched())
				left = left.getMatched();
			if(right != null && right.isMatched())
				right = right.getMatched();
			sb.append(",L:");
			System.out.println(parent.getASTNode());
			if(left != null){
				addNodeType(left, sb);
				System.out.println(left.getASTNode());}
			sb.append(",R:");
			if(right != null){
				addNodeType(right, sb);
				System.out.println(right.getASTNode());}

			// sb.append(",C:");
			// if (parent != null) {
			// 	tempList.clear();
			// 	collectClass(parent);
			// 	if (!tempList.isEmpty()) {
			// 		sb.append(TreeUtils.SYM_OPEN);
			// 		for (int i = 0; i < tempList.size(); i++) {
			// 			sb.append(tempList.get(i));
			// 			if (i < tempList.size() - 1) {
			// 				sb.append(", ");
			// 			}
			// 		}
			// 		sb.append(TreeUtils.SYM_CLOSE);
			// 	}
			// }
			
			
			
			return new Context(sb.toString());
		}else{
			return getContext(op.getNode());
		}
	}

	private void collectClass(TreeNode node) {
		if (node.toString().contains("Type") && !tempList.contains(node.getASTNode().toString())) {
			tempList.add(node.getASTNode().toString());
		}
		if (node.children.size() != 0) {
			for (int i = 0; i < node.children.size(); i++) {
				collectClass(node.children.get(i));
			}
		}
	}

	private void addNodeType(TreeNode node, StringBuffer sb){
		sb.append(TreeUtils.getTypeName(node.getType()));
		StructuralPropertyDescriptor desc = null;
		ASTNode astNode = node.getASTNode();
		if(astNode.getParent().getNodeType() == ASTNode.EXPRESSION_STATEMENT)
			desc = astNode.getParent().getLocationInParent();
		else
			desc = astNode.getLocationInParent();
		if(desc != null){
			sb.append(TreeUtils.SYM_OPEN);
			sb.append(desc.getId());
			sb.append(TreeUtils.SYM_CLOSE);
		}
	}

	@Override
	public Context getContext(TreeNode node){
		StringBuffer sb = new StringBuffer();
		sb.append("P:");
		TreeNode parent = null;
		if(node.getParent() != null && node.getParent().getType() == ASTNode.BLOCK){
			parent = node.getParent().getParent();
		}else{
			parent = node.getParent();
		}
		if(parent != null){
			sb.append(TreeUtils.getTypeName(parent.getType()));
			StructuralPropertyDescriptor desc = null;
			ASTNode astNode = node.getASTNode();
			if(astNode.getParent().getNodeType() == ASTNode.EXPRESSION_STATEMENT)
				desc = astNode.getParent().getLocationInParent();
			else
				desc = astNode.getLocationInParent();
			if(desc != null){
				sb.append(TreeUtils.SYM_OPEN);
				sb.append(desc.getId());
				sb.append(TreeUtils.SYM_CLOSE);
			}
		}
		TreeNode left = node.getLeft();
		TreeNode right = node.getRight();
		sb.append(",L:");
		if(left != null)
			addNodeType(left, sb);
		sb.append(",R:");
		if(right != null)
			addNodeType(right, sb);
		// sb.append(",C:");
		// if (parent != null) {
		// 	tempList.clear();
		// 	collectClass(parent);
		// 	if (!tempList.isEmpty()) {
		// 		sb.append(TreeUtils.SYM_OPEN);
		// 		for (int i = 0; i < tempList.size(); i++) {
		// 			sb.append(tempList.get(i));
		// 			if (i < tempList.size() - 1) {
		// 				sb.append(", ");
		// 			}
		// 		}
		// 		sb.append(TreeUtils.SYM_CLOSE);
		// 	}
		// }
		return new Context(sb.toString());
	}

	@Override
	public Context getContext(Node node) {
		StringBuffer sb = new StringBuffer();
		sb.append("P:");
		Node parent = node.parent != null && node.parent.type == ASTNode.BLOCK ? node.parent.parent : node.parent;
		sb.append(getParentType(node, parent));
		if(parent != null && node.desc != null){
			sb.append(TreeUtils.SYM_OPEN);
			sb.append(node.desc.id);
			sb.append(TreeUtils.SYM_CLOSE);
		}
		Node left = node.getLeft();
		sb.append(",L:");
		if(left != null)
			addNodeType(left, sb);
		sb.append(",R:");
		//Find the first unmatched right node.
		Node right = node.getRight();
		while(right != null && !right.isMatched)
			right = right.getRight();
		if(right != null)
			addNodeType(right, sb);
		// sb.append(",C:");
		// if (parent != null) {
		// 	tempList.clear();
		// 	collectClass(parent.);
		// 	if (!tempList.isEmpty()) {
		// 		sb.append(TreeUtils.SYM_OPEN);
		// 		for (int i = 0; i < tempList.size(); i++) {
		// 			sb.append(tempList.get(i));
		// 			if (i < tempList.size() - 1) {
		// 				sb.append(", ");
		// 			}
		// 		}
		// 		sb.append(TreeUtils.SYM_CLOSE);
		// 	}
		// }
		return new Context(sb.toString());
	}

	@Override
	public Context getLeftContext(Node node) {
		Node left = node.getLeft();
		StringBuffer sb = new StringBuffer();
		sb.append("P:");
		Node parent = node.parent != null && node.parent.type == ASTNode.BLOCK ? node.parent.parent : node.parent;
		sb.append(getParentType(node, parent));
		if(parent != null && node.desc != null){
			sb.append(TreeUtils.SYM_OPEN);
			if(parent.type == ASTNode.INFIX_EXPRESSION) {
				if(node.desc.id.equals(InfixExpression.RIGHT_OPERAND_PROPERTY.getId()))
					sb.append(InfixExpression.LEFT_OPERAND_PROPERTY.getId());
				else if(node.desc.id.equals(InfixExpression.EXTENDED_OPERANDS_PROPERTY.getId()))
					sb.append(InfixExpression.RIGHT_OPERAND_PROPERTY.getId());
				else
					sb.append(node.desc.id);
			} else if(parent.type == ASTNode.METHOD_INVOCATION
					&& node.desc.id.equals(MethodInvocation.NAME_PROPERTY.getId())) {
				sb.append(MethodInvocation.EXPRESSION_PROPERTY.getId());
			} else {
				sb.append(node.desc.id);
			}
			sb.append(TreeUtils.SYM_CLOSE);
		}
		sb.append(",L:");
		if(left != null)
			addNodeType(left, sb);
		sb.append(",R:");
		addNodeType(node, sb);
		return new Context(sb.toString());
	}

	@Override
	public Context getRightContext(Node node){
		Node right = node.getRight();
		StringBuffer sb = new StringBuffer();
		sb.append("P:");
		Node parent = node.parent != null && node.parent.type == ASTNode.BLOCK ? node.parent.parent : node.parent;
		sb.append(getParentType(node, parent));
		if(parent != null && node.desc != null){
			sb.append(TreeUtils.SYM_OPEN);
			if(parent.type == ASTNode.INFIX_EXPRESSION) {
				if(node.desc.id.equals(InfixExpression.LEFT_OPERAND_PROPERTY.getId()))
					sb.append(InfixExpression.RIGHT_OPERAND_PROPERTY.getId());
				else if(node.desc.id.equals(InfixExpression.RIGHT_OPERAND_PROPERTY.getId()))
					sb.append(InfixExpression.EXTENDED_OPERANDS_PROPERTY.getId());
				else
					sb.append(node.desc.id);
			} else if(parent.type == ASTNode.IF_STATEMENT
					&& node.desc.id.equals(IfStatement.THEN_STATEMENT_PROPERTY.getId())) {
				sb.append(IfStatement.ELSE_STATEMENT_PROPERTY.getId());
			} else {
				sb.append(node.desc.id);
			}
			sb.append(TreeUtils.SYM_CLOSE);
		}
		sb.append(",L:");
		addNodeType(node, sb);
		sb.append(",R:");
		if(right != null)
			addNodeType(right, sb);
		return new Context(sb.toString());
	}
}
