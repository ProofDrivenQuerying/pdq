<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<%@include file="head.jsp"%>
<body>
	<div id="content">
		<%@include file="header.jsp"%>
		<%@include file="menu.jsp"%>
		<div id="main">
			<div id="overview">
				<h2>Overview</h2>
				<p>
					The data needed to answer queries is often available through Web-based APIs.
					For a given query there may be many Web-based sources which can be used to answer it,
					with the sources overlapping in their vocabularies,
					and differing in their access restrictions (required arguments) and cost.
					We introduce PDQ (Proof-Driven Query Answering),
					a system for determining a query plan in the presence of web-based sources.
					It is:
						(i) constraint-aware &mdash;
							exploiting relationships between sources
							to rewrite an expensive query into a cheaper one,
						(ii) access-aware &mdash;
							abiding by any access restrictions known in the sources, and
						(iii) cost-aware &mdash;
							making use of any cost information that is available about services.
							PDQ takes the novel approach of generating query plans from proofs that a query is answerable.
				</p>
				<p>
					PDQ comes with a user interface, described in detail in this
					<a href="#screencast">video</a>, and a <a href="demo.jsp">web front-end</a>
					for demonstration and research purposes.
				</p>
			</div>

			<div id="people">
				<h2>People</h2>
				<ul>
					<li><a href="http://www.cs.ox.ac.uk/people/michael.benedikt/">Michael Benedikt</a> &ndash; Principal investigator</li>
					<li><a href="http://www.cs.ox.ac.uk/people/efthymia.tsamoura/">Efthymia Tsamoura</a> &ndash; Research Assistant</li>
					<li><a href="http://www.cs.ox.ac.uk/people/julien.leblay/">Julien Leblay</a> &ndash; Research Assistant</li>
				</ul>
			</div>

			<div id="references">
				<h2>References</h2>
				<ul>
					<li>
						<span class="title"><a href="http://www.cs.ox.ac.uk/people/julien.leblay/share/access.pdf">Generating Low-cost Plans From Proofs</a></span> - <br />
						<span class="authors">Michael Benedikt, Balder ten Cate and Efthymia Tsamoura</span>,
						<span class="venue">PODS 2014</span>.
					</li>
					<li>
						<span class="title"><a href="http://www.cs.ox.ac.uk/people/julien.leblay/share/p1029-benedikt.pdf">PDQ: Proof-driven Query Answering over Web-based Data</a></span> - <br />
						<span class="authors">Michael Benedikt, Julien Leblay and Efthymia Tsamoura</span>,
						<span class="venue">VLDB 2014 (demo)</span>.
					</li>
					<li>
						<span class="title"><a href="http://www.cs.ox.ac.uk/files/7031/p549-benedikt.pdf">Querying with Access Patterns and Integrity Constraints</a></span> - <br />
						<span class="authors">Michael Benedikt, Julien Leblay and Efthymia Tsamoura</span>,
						<span class="venue">VLDB 2015</span>
						<span >(see also our <a href="http://www.cs.ox.ac.uk/projects/pdq/experiments/">experiments sites<a/>).</span>
					</li>
				</ul>
			</div>

			<div id="documentation">
				<h2>Documentation</h2>
				<h3>Web front-end</h3>
				<p>
					The <a href="demo.jsp">web front-end</a> works by taking schema and query files as inputs.
					You can find more details on how to create <a href="doc/schema.jsp">schema</a> and <a href="doc/query.jsp">query</a> files.
				</p>
				
				
    			<h3>API</h3>
	    		<p>
	    			The PDQ sources can be download from <a href="sources/sources.zip">here</a>. Users can also directly download the pre-built 
	    			<a href="release/pdq-benchmark-1.0.0-SNAPSHOT.one-jar">.jar</a>. A quick introduction to plan creation and plan execution using PDQ can be found 
	    			<a href="doc/tutorial_planning.jsp">here</a> and <a href="doc/tutorial_runtime.jsp">here</a>, while 
	    			<a href="doc/tutorial_build.txt">this tutorial</a> describe the steps to build PDQ from its sources. 
	    		</p>	    		
				
				
				<h3>User interface</h3>
				<p>
					The user interface can be downloaded from <a href="http://www.cs.ox.ac.uk/people/julien.leblay/share/pdq-demo.jar">here</a>.
					The following video presents a walk-through to the user interface.
				</p>
				
				
				<p>
					<a id="screencast"></a>
					<video width="600" controls>
						<source src="http://www.cs.ox.ac.uk/people/michael.benedikt/pdq/screencast.mp4" type="video/mp4" />
						<source src="http://www.cs.ox.ac.uk/people/julien.leblay/share/pdq-screencast.ogg" type="video/ogg" />
						Your browser does not support video embedding.
						You can download the video <a href="http://www.cs.ox.ac.uk/people/michael.benedikt/pdq/screencast.mp4">here</a>.
					</video>
				</p>
				<br/>
				<br/>
				<br/>
				<br/>
				<br/>
				<br/>
			</div>
		</div>
		<%@include file="footer.jsp"%>
	</div>
</body>
</html>
