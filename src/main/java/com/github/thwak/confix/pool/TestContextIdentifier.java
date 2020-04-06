package com.github.thwak.confix.pool;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.thwak.confix.tree.Node;
import com.github.thwak.confix.tree.Parser;
import com.github.thwak.confix.tree.TreeUtils;

import script.model.EditOp;
import tree.TreeNode;

public class TestContextIdentifier extends ContextIdentifier {

	private static final long serialVersionUID = -8352611691723991826L;
	private ArrayList<String> tempList = new ArrayList<>();
	private ArrayList<String> nameList = new ArrayList<>();

	@Override
	public Context getContext(EditOp op, File aFile) {
		if(op.getType().equals(Change.INSERT)){
			StringBuffer sb = new StringBuffer();
			sb.append("P:");
			TreeNode parent = null;
			parent = op.getNode().getParent();
			if(parent.getType() == ASTNode.BLOCK && parent.getParent() != null)
				parent = parent.getParent();
			if(parent != null){
				if(parent.getMatched() != null){
					parent = parent.getMatched();
				}
				// System.out.println("parent: " + parent.getASTNode());
				// for(int i = 0; i < parent.children.size(); i++) {
				// 	System.out.println("parent1:" + parent.children.get(i).getASTNode());
				// 	System.out.println("parent1name:" + parent.children.get(i).getLabel());
				// 	TreeNode child = parent.children.get(i);
				// 	for(int j = 0; j < child.children.size(); j++) {
				// 		System.out.println("parent2:" + child.children.get(j).getASTNode());
				// 		System.out.println("parent2name:" + child.children.get(j).getLabel());
				// 		TreeNode child2 = child.children.get(j);
				// 		for(int k = 0; k < child2.children.size(); k++) {
				// 			System.out.println("parent3:" + child2.children.get(k).getASTNode());
				// 			System.out.println("parent3name:" + child2.children.get(k).getLabel());
				// 			TreeNode child3 = child2.children.get(k);
				// 			for(int l = 0; l < child3.children.size(); l++) {
				// 				System.out.println("parent4:" + child3.children.get(l).getASTNode());
				// 				System.out.println("parent4name:" + child3.children.get(l).getLabel());
				// 				TreeNode child4 = child3.children.get(k);
				// 				for(int m = 0; m < child4.children.size(); m++) {
				// 					System.out.println("parent5:" + child4.children.get(m).getASTNode());
				// 					System.out.println("parent5name:" + child4.children.get(m).getLabel());
				// 				}
				// 			}
				// 		}
				// 	}
				// }
				nameList.clear();
				extractNameinContext(parent);
				// for (int i = 0; i < nameList.size(); i++) {
				// 	System.out.println(i + ":" + nameList.get(i));
				// }

				
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
			if(left != null) {
				addNodeType(left, sb);
				// System.out.println("left:" + left.getASTNode());
			}
			sb.append(",R:");
			if(right != null) {
				addNodeType(right, sb);
				// System.out.println("right:" + right.getASTNode());
			}

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

			//extract procedure context info.
			// System.out.println("name:" + aFile.getName());
			// System.out.println("string:" + aFile.getAbsolutePath());
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			String line = "";
			String fileLine = "";
			try {
				BufferedReader bufReader = new BufferedReader(new FileReader(aFile));
						
				while ((line = bufReader.readLine()) != null) {
					fileLine = fileLine.concat(line+"\n");
				}
				bufReader.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			parser.setSource(fileLine.toCharArray());
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			CompilationUnit cu = (CompilationUnit) parser.createAST(null);

			// System.out.println("----------------- info -------------------");
			// System.out.println(cu.getStartPosition());
			// System.out.println(cu.getLineNumber(cu.getStartPosition()));

			ASTPrint(cu, cu.getRoot());

			return new Context(sb.toString());
		}else{
			return getContext(op.getNode());
		}
	}
	private void ASTPrint(CompilationUnit cu, ASTNode node) {
		System.out.println(
			cu.getLineNumber(node.getStartPosition()) + "\t" +
			node.toString()
			);
		List properties = node.structuralPropertiesForType();
		for (Iterator iterator = properties.iterator(); iterator.hasNext();) {
			Object descriptor = iterator.next();
			if (descriptor instanceof SimplePropertyDescriptor) {
				SimplePropertyDescriptor simple = (SimplePropertyDescriptor) descriptor;
				Object value = node.getStructuralProperty(simple);
				if (value == null) {
					System.out.println(simple.getId() + " (" + value + ")");
				} else {
					System.out.println(simple.getId() + " (" + value.toString() + ")");
				}
				
			} else if (descriptor instanceof ChildPropertyDescriptor) {
				ChildPropertyDescriptor child = (ChildPropertyDescriptor) descriptor;
				ASTNode childNode = (ASTNode) node.getStructuralProperty(child);
				if (childNode != null) {
					System.out.println("Child (" + child.getId() + ") {");
					ASTPrint(cu, childNode);
					System.out.println("}");
				}
			} else {
				ChildListPropertyDescriptor list = (ChildListPropertyDescriptor) descriptor;
				System.out.println("List (" + list.getId() + "){");
				ASTPrint(cu, (List) node.getStructuralProperty(list));
				System.out.println("}");
			}
		}
	}
	
	private void ASTPrint(CompilationUnit cu, List nodes) {
		for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
			ASTPrint(cu, (ASTNode) iterator.next());
		}
	}
	@Override
	public Context getContext(EditOp op) {
		if(op.getType().equals(Change.INSERT)){
			StringBuffer sb = new StringBuffer();
			sb.append("P:");
			TreeNode parent = null;
			
			parent = op.getNode().getParent();
			if(parent.getType() == ASTNode.BLOCK && parent.getParent() != null)
				parent = parent.getParent();
			
			System.out.println("op: " + op.toOpString());

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
			if(left != null)
				addNodeType(left, sb);
			sb.append(",R:");
			if(right != null)
				addNodeType(right, sb);			
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

	private void extractNameinContext(TreeNode node) {
		if (node.getLabel().contains("Declaration")
				|| node.getLabel().contains("Assignment")
				|| node.getLabel().contains("Invocation")) {
			if (node.getLabel().contains("Assignment")) {
				System.out.println("----assignment-----\n" + node.getASTNode());
			}
			for (int i = 0; i < node.children.size(); i++) {
				getNameinContext(node.children.get(i));
			}
		} else {
			for(int i = 0; i < node.children.size(); i++) {
				extractNameinContext(node.children.get(i));
			}
		}
	}

	private void getNameinContext(TreeNode node) {
		if (node.getLabel().contains("Name")) {
			nameList.add(node.getASTNode().toString());
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
