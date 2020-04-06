package com.github.thwak.confix.pool;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
	private int jsonNodeId = 0;
	JSONObject jsonObject = new JSONObject();
	JSONArray nodeArray = new JSONArray();
	JSONArray edgeArray = new JSONArray();

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
				nameList.clear();
				extractNameinContext(parent);
				
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
			try {
				BufferedWriter bufWriter = new BufferedWriter(new FileWriter(new File("/home/hjsvm/hjsaprvm/jsontest.txt")));
				ASTtoJsonPrint(cu, cu.getRoot(), bufWriter, 0, "");
				jsonObject.put("node", nodeArray);
				jsonObject.put("edge", edgeArray);
				bufWriter.close();
				bufWriter = new BufferedWriter(new FileWriter(new File("/home/hjsvm/hjsaprvm/realjson.txt")));
				bufWriter.write(jsonObject.toJSONString());
				bufWriter.close();
			} catch (Exception e) {
			}
			return new Context(sb.toString());
		}else{
			return getContext(op.getNode());
		}
	}

	private void ASTtoJsonPrint(CompilationUnit cu, ASTNode node, BufferedWriter bufWriter, int parentNodeId, String typeName) throws IOException {
		int thisNodeId = ++jsonNodeId;
		JSONObject jsonEdge = new JSONObject();
		JSONObject jsonNode = new JSONObject();

		jsonEdge.put("parent", parentNodeId);
		jsonEdge.put("child", thisNodeId);
		jsonNode.put("id", thisNodeId);
		jsonNode.put("contents", node.toString());
		jsonNode.put("type", typeName);
		jsonNode.put("linenum", cu.getLineNumber(node.getStartPosition()));
		edgeArray.add(jsonEdge);
		nodeArray.add(jsonNode);

		bufWriter.write("edge:(" + Integer.toString(parentNodeId) + ", " + Integer.toString(thisNodeId) + ")\n");
		bufWriter.write("nodeid: " + Integer.toString(thisNodeId) + "\n");
		bufWriter.write("nodecontents: " + node.toString() + "\n");
		bufWriter.write("nodetype:" + typeName + "\n");
		bufWriter.write("nodelinenum:" + cu.getLineNumber(node.getStartPosition()) + "\n");
		List properties = node.structuralPropertiesForType();
		for (Iterator iterator = properties.iterator(); iterator.hasNext();) {
			Object descriptor = iterator.next();
			if (descriptor instanceof SimplePropertyDescriptor) {
				SimplePropertyDescriptor simple = (SimplePropertyDescriptor) descriptor;
				Object value = node.getStructuralProperty(simple);
				if (value == null) {
					// bufWriter.write("-------type1--------\n");
					// bufWriter.write(simple.getId() + " (" + value + ")" + "\n");
					// bufWriter.write("------------------\n");
				} else {
					// bufWriter.write("-------type2--------\n");
					// bufWriter.write(simple.getId() + " (" + value.toString() + ")" + "\n");
					// bufWriter.write("------------------\n");
				}
			} else if (descriptor instanceof ChildPropertyDescriptor) {
				ChildPropertyDescriptor child = (ChildPropertyDescriptor) descriptor;
				ASTNode childNode = (ASTNode) node.getStructuralProperty(child);
				if (childNode != null) {
					bufWriter.write("Child (" + child.getId() + ") {" + "\n");
					ASTtoJsonPrint(cu, childNode, bufWriter, thisNodeId, child.getId());
					bufWriter.write("}\n");
				}
			} else {
				ChildListPropertyDescriptor list = (ChildListPropertyDescriptor) descriptor;
				bufWriter.write("List (" + list.getId() + "){" + "\n");
				ASTtoJsonPrint(cu, (List) node.getStructuralProperty(list), bufWriter, thisNodeId, list.getId());
				bufWriter.write("}\n");
			}
		}
	}
	
	private void ASTtoJsonPrint(CompilationUnit cu, List nodes, BufferedWriter bufWriter, int parentNodeId, String typeName) throws IOException {
		for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
			ASTtoJsonPrint(cu, (ASTNode) iterator.next(), bufWriter, parentNodeId, typeName);
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
